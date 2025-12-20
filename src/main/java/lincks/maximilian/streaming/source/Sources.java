package lincks.maximilian.streaming.source;

import static lincks.maximilian.streaming.stage.Stages.buffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
}
