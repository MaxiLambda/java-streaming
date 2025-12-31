package lincks.maximilian.streaming.source;

import static lincks.maximilian.streaming.stage.Stages.*;
import static lincks.maximilian.util.Util.cleanup;

import java.util.*;
import java.util.function.*;
import lincks.maximilian.functional.tuple.Tuple;
import lincks.maximilian.util.Mutable;

public interface Sources {

  /** Creates a new Source based on the given Iterable. */
  static <T> Source<T> fromIterable(Iterable<T> iterable) {
    return fromIterator(iterable.iterator());
  }

  /** Creates a new Source based on the given Iterator. */
  static <T> Source<T> fromIterator(Iterator<T> iterator) {
    return () -> {
      if (iterator.hasNext()) {
        return Optional.of(iterator.next());
      } else {
        return Optional.empty();
      }
    };
  }

  /**
   * Can be used to cast lambdas, to add typing to anonymous stage implementations and to clarify
   * the intent of anonymous {@link Source} implementations.
   *
   * <pre>{@code
   * //the compiler infers this as Stage<Object, Object>
   * //if this is used in a chain of Stages, Stage<Integer, Object> might be inferred
   * (s) -> () -> s.pull().map(i -> i + 1)
   *
   * // using enables the compiler to infer the type of the inner lambda and type the stage correctly
   * (s) -> source(() -> s.pull().map(i -> i + 1))
   * }</pre>
   *
   * @param source usually a lambda implementing {@link Source}
   * @return a {@link Source}
   * @param <T> the type of the source.
   */
  static <T> Source<T> source(Source<T> source) {
    return source;
  }

  @SafeVarargs
  static <T> Source<T> concat(Source<T>... sources) {
    return Sources.fromIterable(new ArrayList<>(List.of(sources))).then(buffer());
  }

  static <T1, T2, R> Source<R> zip(
      BiFunction<T1, T2, R> zipper, Source<T1> first, Source<T2> second) {
    return () ->
        first
            .pull()
            .flatMap(firstVal -> second.pull().map(secondVal -> zipper.apply(firstVal, secondVal)));
  }

  static <T> Source<T> cycle(Source<T> source) {
    return new Source<T>() {
      final Source<T> initialSource = source;
      final ArrayDeque<T> cycleBuffer = new ArrayDeque<>();
      boolean depleted = false;

      @Override
      public Optional<T> pull() {
       if(depleted) {
         var next = cycleBuffer.removeFirst();
         cycleBuffer.addLast(next);
         return Optional.of(next);
       } else {
         var nextCandidate = initialSource.pull();
         if (nextCandidate.isPresent()) {
           cycleBuffer.addLast(nextCandidate.get());
           return nextCandidate;
         }
         depleted = true;
         return pull();
       }
      }
    };
  }

  static <T, R> Source<R> unfold(Function<T, Optional<Tuple<R, T>>> unfolder, Supplier<T> seed) {
    return new Source<>() {
      T state = seed.get();

      @Override
      public Optional<R> pull() {
        var next = unfolder.apply(state);
        next.map(Tuple::snd).ifPresent(state -> this.state = state);
        return next.map(Tuple::fst);
      }
    };
  }

  static <T> Source<T> iterate(Supplier<T> initial, UnaryOperator<T> f) {
    Mutable<T> state = new Mutable<>(initial.get());
    return () -> cleanup(() -> Optional.of(state.get()), () -> state.mutateWith(f));
  }

  static <T> Source<T> fetchWhile(Supplier<T> source, Predicate<T> predicate) {
    return source(() -> Optional.of(source.get())).then(takeWhile(predicate));
  }

  static <T> Source<T> infinite(Supplier<T> element) {
    return () -> Optional.of(element.get());
  }

  static <T> Source<T> repeat(Supplier<T> element, int times) {
    return infinite(element).then(take(times));
  }
}
