package lincks.maximilian.streaming.sink;

import static lincks.maximilian.streaming.interop.StreamInterop.toSourceCollector;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.stage.Stage;

/**
 * Sinks are terminal operations on {@link Source}s. But they can be used as stages on nested
 * sources as well.
 */
public interface Sink<T, R> {

  /** Drains a {@link Source} and reduces its content into a single value. */
  R collect(Source<T> source);
}
