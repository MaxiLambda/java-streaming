package lincks.maximilian.streaming.stage;

import lincks.maximilian.streaming.sink.Sink;
import lincks.maximilian.streaming.source.Source;

public interface Stage<T, R> {

  /**
   * Creates a new {@link Source} from a given {@link Source}. May merge multiple Elements from the
   * Source and/ot transform them.
   */
  Source<R> setup(Source<T> source);

  /** Composes two {@link Stage}s into a new Stage. */
  default <RR> Stage<T, RR> then(Stage<R, RR> next) {
    return (source) -> next.setup(setup(source));
  }

  /**
   * Creates a new {@link Sink} by preprocessing all values before feeding them to the given Sink.
   */
  default <RR> Sink<T, RR> reduce(Sink<R, RR> sink) {
    return (source) -> this.setup(source).reduce(sink);
  }}
