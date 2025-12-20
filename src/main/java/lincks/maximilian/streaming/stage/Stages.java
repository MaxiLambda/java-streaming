package lincks.maximilian.streaming.stage;

import static lincks.maximilian.streaming.source.Sources.fromIterable;
import static lincks.maximilian.streaming.stage.StageChain.$;
import static lincks.maximilian.util.Util.fluent;
import static lincks.maximilian.util.Util.ignore;

import java.util.*;
import java.util.function.*;
import lincks.maximilian.streaming.sink.Sink;
import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.util.Mutable;

public interface Stages {
  static <T> Stage<T, T> identity() {
    return source -> source;
  }

  static <T, R> Stage<T, R> map(Function<T, R> transformer) {
    // sematically equivalent implementations, but the later is more efficient
    // return integrate(ignore(), (val, _) -> State.of(transformer.apply(val)));
    return source -> () -> source.pull().map(transformer);
  }

  static <T, R> Stage<T, R> flatMap(Function<T, Source<R>> transformer) {
    return map(transformer).then(buffer());
  }

  static <T> Stage<T, T> filter(Predicate<T> predicate) {
    return integrate(
        ignore(), (val, _) -> predicate.test(val) ? State.of(val) : State.of(Optional.empty()));
  }

  static <T, R> Stage<T, R> mapOptional(Function<T, Optional<R>> transformer) {
    return $(map(transformer), filter(Optional::isPresent), map(Optional::orElseThrow));
  }

  static <T> Stage<T, T> dropWhile(Predicate<T> predicate) {
    return integrate(
        () -> false,
        (val, take) ->
            take || !predicate.test(val) ? State.of(true, val) : State.of(false, Optional.empty()));
  }

  static <T> Stage<T, T> takeWhile(Predicate<T> predicate) {
    return integrate(
        ignore(), (val, acc) -> predicate.test(val) ? State.of(val) : State.exitEmpty());
  }

  static <T> Stage<T, T> limit(int limit) {
    return integrate(
        () -> 0, (val, passed) -> passed < limit ? State.of(passed + 1, val) : State.exitEmpty());
  }

  static <T> Stage<Source<T>, T> buffer() {
    return source ->
        new Source<>() {
          private Source<T> bufferedSource = Optional::empty;

          @Override
          public Optional<T> pull() {
            {
              Optional<T> token = bufferedSource.pull();
              if (token.isEmpty()) {
                Optional<Source<T>> sourceToken = source.pull();
                if (sourceToken.isEmpty()) {
                  return Optional.empty();
                } else {
                  bufferedSource = sourceToken.get();
                  return pull();
                }
              } else {
                return token;
              }
            }
          }
        };
  }

  static <T, R> Stage<Source<T>, R> mapInner(Sink<T, R> sink) {
    // sematically equivalent implementations, but the later is more efficient
    // return integrate(ignore(), (val, _) -> State.of(val.reduce(sink)));
    return (source) -> () -> source.pull().map(s -> s.reduce(sink));
  }

  /**
   * Split the Source into sequences of length size. If not enough values are present, the last
   * group may be smaller than size.
   */
  static <T> Stage<T, Source<T>> groupsOf(int size) {
    return Stages.integrate(
        ArrayList<T>::new,
        (val, acc) -> {
          acc.add(val);
          if (acc.size() < size) return State.of(acc, Optional.empty());
          return State.of(new ArrayList<>(), fromIterable(acc));
        },
        acc -> acc.isEmpty() ? Optional.empty() : Optional.of(fromIterable(acc)));
  }

  static <T> Stage<T, Source<T>> groupsOfExact(int size) {
    return Stages.integrate(
        ArrayList<T>::new,
        (val, acc) -> {
          acc.add(val);
          if (acc.size() < size) return State.of(acc, Optional.empty());
          var ret = fromIterable(new ArrayList<>(acc));
          return State.of(new ArrayList<>(), ret);
        });
  }

  static <T> Stage<T, Source<T>> slidingWindow(int size) {

    return Stages.integrate(
        ArrayDeque<T>::new,
        (val, acc) -> {
          acc.addLast(val);
          if (acc.size() < size) return State.of(acc, Optional.empty());
          var ret = fromIterable(new ArrayList<>(acc));
          return State.of(fluent(acc, ArrayDeque::removeFirst), ret);
        });
  }

  record State<A, R>(A acc, Optional<R> result, boolean exit) {

    public static <A, R> State<A, R> exitWith(Optional<R> result) {
      return new State<>(null, result, true);
    }

    public static <A, R> State<A, R> exitEmpty() {
      return new State<>(null, Optional.empty(), true);
    }

    public static <A, R> State<A, R> of(A acc, R result) {
      return new State<>(acc, Optional.of(result), false);
    }

    public static <A, R> State<A, R> of(A acc, Optional<R> result) {
      return new State<>(acc, result, false);
    }

    public static <R> State<Void, R> of(Optional<R> result) {
      return new State<>(null, result, false);
    }

    public static <R> State<Void, R> of(R result) {
      return new State<>(null, Optional.of(result), false);
    }
  }

  static <T, A, R> Stage<T, R> integrate(
      Supplier<A> initializer, BiFunction<T, A, State<A, R>> accumulator) {
    return integrate(initializer, accumulator, _ -> Optional.empty());
  }

  /* Offers a functional way to write Stages.
   * The initializer is used to provide the initial value of the accumulator.
   * The accumulator function is used to combine the state from possibly many values from upstream.
   * The finisher runs on the last State of the accumulator when upstream runs try. The finisher may or may not return an additional value.
   *  */
  static <T, A, R> Stage<T, R> integrate(
      Supplier<A> initializer,
      BiFunction<T, A, State<A, R>> accumulator,
      Function<A, Optional<R>> finisher) {
    // Mutable's are required because references are not allowed to change inside lambdas
    Mutable<State<A, R>> state = new Mutable<>(State.of(initializer.get(), Optional.empty()));
    Mutable<Boolean> finisherRan = new Mutable<>(false);
    return source ->
        // pull method of the returned source
        () -> {
          // if state.exit is true, this stage will pass no further values downstream
          if (state.get().exit) return state.get().result;
          // when downstream pulls, it might be necessary to pull multiple times upstream to create
          // a new value to push down.
          // state is persisted in a State holding an accumulator A and a result
          while (state.get().result.isEmpty()) {
            Optional<T> token = source.pull(); // pull from downstream
            // check if downstream is empty
            if (token.isEmpty()) {
              // state contains no valid result
              if (!finisherRan.get()) {
                // A finisher can be used to turn the current state of the accumulator into another
                // valid result. It is necessary to use a flag to ensure the finisher can only be
                // used once.
                finisherRan.set(true);
                return finisher.apply(state.get().acc);
              }
              return Optional.empty();
            }
            // run the accumulator and update the current state
            state.set(accumulator.apply(token.get(), state.get().acc));
            // if state.exit is true, this stage will pass no further values downstream
            if (state.get().exit) return state.get().result;
          }
          // save the result before cleaning the State
          var res = state.get().result;
          // clean up the result to prevent loops
          state.set(State.of(state.get().acc, Optional.empty()));
          return res;
        };
  }
}
