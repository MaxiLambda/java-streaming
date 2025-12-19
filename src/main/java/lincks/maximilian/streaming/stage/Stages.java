package lincks.maximilian.streaming.stage;

import static lincks.maximilian.streaming.source.Sources.fromIterable;
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
    return source -> () -> source.pull().map(transformer);
  }

  static <T, R> Stage<T, R> flatMap(Function<T, Source<R>> transformer) {
    return map(transformer).then(buffer());
  }

  static <T> Stage<T, T> filter(Predicate<T> predicate) {
    return source ->
        () -> {
          Optional<T> val = source.pull();
          // return empty if source is empty
          while (val.isPresent()) {
            if (predicate.test(val.get())) {
              // only return val when it satisfies the predicate
              return val;
            }
            val = source.pull();
          }
          return Optional.empty();
        };
  }

  static <T, R> Stage<T, R> mapOptional(Function<T, Optional<R>> transformer) {
    return map(transformer).then(filter(Optional::isPresent)).then(map(Optional::orElseThrow));
  }

  static <T> Stage<T, T> takeWhile(Predicate<T> predicate) {
    return source ->
        () ->
            // pulls a new value and checks if it matches the predicate
            // if yes -> the value is returned
            // if no  -> Optional.empty is returned, effectively signaling that no further
            // processing is required
            source.pull().flatMap(val -> predicate.test(val) ? Optional.of(val) : Optional.empty());
  }

  static <T> Stage<T, T> limit(int limit) {
    return new Stage<>() {
      private int counter = 0;

      @Override
      public Source<T> setup(Source<T> source) {
        return () -> {
          if (limit < ++counter) {
            return Optional.empty();
          } else {
            return source.pull();
          }
        };
      }
    };
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
    // equivalent implementations
//     return integrate(ignore(), (val, _) -> State.of(val.reduce(sink)));
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
          if (acc.size() < size) return new State<>(acc, Optional.empty());
          return new State<>(new ArrayList<>(), Optional.of(fromIterable(acc)));
        },
        acc -> acc.isEmpty() ? Optional.empty() : Optional.of(fromIterable(acc)));
  }

  static <T> Stage<T, Source<T>> groupsOfExact(int size) {
    return Stages.integrate(
        ArrayList<T>::new,
        (val, acc) -> {
          acc.add(val);
          if (acc.size() < size) return new State<>(acc, Optional.empty());
          var ret = Optional.of(fromIterable(new ArrayList<>(acc)));
          return new State<>(new ArrayList<>(), ret);
        });
  }

  static <T> Stage<T, Source<T>> slidingWindow(int size) {

    return Stages.integrate(
        ArrayDeque<T>::new,
        (val, acc) -> {
          acc.addLast(val);
          if (acc.size() < size) return new State<>(acc, Optional.empty());
          var ret = Optional.of(fromIterable(new ArrayList<>(acc)));
          return new State<>(fluent(acc, ArrayDeque::removeFirst), ret);
        });
  }

  record State<A, R>(A acc, Optional<R> result) {
    static <R> State<Void, R> of(R result) {
      return new State<>(null, Optional.of(result));
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
    Mutable<State<A, R>> state = new Mutable<>(new State<>(initializer.get(), Optional.empty()));
    Mutable<Boolean> finisherRan = new Mutable<>(false);
    return source ->
        // pull method of the returned source
        () -> {
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
          }
          // save the result before cleaning the State
          var res = state.get().result;
          // clean up the result to prevent loops
          state.set(new State<>(state.get().acc, Optional.empty()));
          return res;
        };
  }

  // can be used to cast lambdas
  static <T, R> Stage<T, R> $(Stage<T, R> first) {
    return first;
  }

  static <T, A, R> Stage<T, R> $(Stage<T, A> first, Stage<A, R> second) {
    return first.then(second);
  }

  static <T, T2, T3, R> Stage<T, R> $(
      Stage<T, T2> first, Stage<T2, T3> second, Stage<T3, R> third) {
    return first.then(second).then(third);
  }

  static <T, T2, T3, T4, R> Stage<T, R> $(
      Stage<T, T2> first, Stage<T2, T3> second, Stage<T3, T4> third, Stage<T4, R> fourth) {
    return first.then(second).then(third).then(fourth);
  }

  static <T, T2, T3, T4, T5, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, R> fifth) {
    return first.then(second).then(third).then(fourth).then(fifth);
  }

  static <T, T2, T3, T4, T5, T6, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, T6> fifth,
      Stage<T6, R> sixth) {
    return first.then(second).then(third).then(fourth).then(fifth).then(sixth);
  }

  static <T, T2, T3, T4, T5, T6, T7, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, T6> fifth,
      Stage<T6, T7> sixth,
      Stage<T7, R> seventh) {
    return first.then(second).then(third).then(fourth).then(fifth).then(sixth).then(seventh);
  }

  static <T, T2, T3, T4, T5, T6, T7, T8, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, T6> fifth,
      Stage<T6, T7> sixth,
      Stage<T7, T8> seventh,
      Stage<T8, R> eighth) {
    return first
        .then(second)
        .then(third)
        .then(fourth)
        .then(fifth)
        .then(sixth)
        .then(seventh)
        .then(eighth);
  }
}
