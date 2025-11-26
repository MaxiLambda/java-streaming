package lincks.maximilian.streaming;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import static lincks.maximilian.streaming.Sources.sourceCollector;

/** Sinks are usually terminal operations, but they can be used as stages on nested sources. */
public interface Sink<T, R> extends Stage<Source<T>, R> {

  /** Drains a Source into a value. */
  R collect(Source<T> source);

  /** Implements the Stage interface. */
  @Override
  default Source<R> setup(Source<Source<T>> source) {
    return () -> source.pull().map(s -> s.reduce(this));
  }

  default Collector<T, ?, R> toCollector() {
    return Collectors.collectingAndThen(
            sourceCollector(),
            this::collect);
  }
}
