package lincks.maximilian.streaming;

/** Sinks are usually terminal operations, but they can be used as stages on nested sources. */
public interface Sink<T, R> extends Stage<Source<T>, R> {

  /** Drains a Source into a value. */
  R collect(Source<T> source);

  /** Implements the Stage interface. */
  @Override
  default Source<R> setup(Source<Source<T>> source) {
    return () -> source.pull().map(s -> s.reduce(this));
  }
}
