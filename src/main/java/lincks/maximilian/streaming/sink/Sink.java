package lincks.maximilian.streaming.sink;

import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.stage.Stage;

import static lincks.maximilian.streaming.source.Sources.toSourceCollector;

import java.util.stream.Collector;
import java.util.stream.Collectors;

/** Sinks are terminal operations on {@link Source}s. But they can be used as stages on nested sources as well. */
public interface Sink<T, R> extends Stage<Source<T>, R> {

  /** Drains a {@link Source} and reduces its content into a single value. */
  R collect(Source<T> source);

  /**
   * Implements the {@link Stage} interface for Stages where the input is a {@link Source}. This
   * enables the usage of Sinks as Stages like: <code>Source&ltSource&ltT&gt&gt nestedSource = ... ;
   * Source&ltList&ltT&gt&gt listSource = nestedSource.then(toList());
   * </code> to handle nested values immediately.
   */
  @Override
  default Source<R> setup(Source<Source<T>> source) {
    return () -> source.pull().map(s -> s.reduce(this));
  }

  /** Creates a {@link Collector} from this Sink. */
  default Collector<T, ?, R> toCollector() {
    return Collectors.collectingAndThen(toSourceCollector(), this::collect);
  }
}
