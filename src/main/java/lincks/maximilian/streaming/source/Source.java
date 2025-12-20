package lincks.maximilian.streaming.source;

import static lincks.maximilian.streaming.source.Sources.fromIterable;

import java.util.*;
import java.util.function.Consumer;
import lincks.maximilian.streaming.sink.Sink;
import lincks.maximilian.streaming.sink.Sinks;
import lincks.maximilian.streaming.stage.Stage;

/**
 * Sources supply {@link Stage}s and {@link Sink}s with values. Sources are stateful and not
 * reusable. DO NOT USE A SOURCE AFTER CALLING ANY METHOD ON IT.
 */
public interface Source<T> extends Iterable<T> {

  /**
   * Fetches a new value from the upstream source or creates a new one.
   *
   * @return Optional with the new value. Optional.empty() when all values are exhausted.
   */
  Optional<T> pull();

  /**
   * Joins two Sources. Once this is depleted, other is used.
   *
   * @return a new Source consisting of this and other.
   */
  default Source<T> concat(Source<T> other) {
    return () -> {
      Optional<T> token = pull();
      if (token.isEmpty()) {
        return other.pull();
      } else {
        return token;
      }
    };
  }

  /**
   * Creates a new Source over the return values of a {@link Stage} by enabling the Stage to pull
   * and process values from this source.
   */
  default <R> Source<R> then(Stage<T, R> next) {
    return next.setup(this);
  }

  /** Drain this source into a {@link Sink} and return the Sinks result. */
  default <RR> RR reduce(Sink<T, RR> sink) {
    return sink.collect(this);
  }

  /** Creates an empty Source. */
  static <T> Source<T> empty() {
    return Optional::empty;
  }

  /** Creates a new Source based on the given elements. */
  @SafeVarargs
  static <T> Source<T> of(T... elements) {
    return fromIterable(Arrays.asList(elements));
  }

  /**
   * Creates an {@link Iterator} from this Source. This DESTROYS this source. DO NOT USE IT
   * AFTERWARD.
   */
  @Override
  default Iterator<T> iterator() {
    return new IteratorSource<>(this);
  }
}
