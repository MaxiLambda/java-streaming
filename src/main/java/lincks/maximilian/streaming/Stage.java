package lincks.maximilian.streaming;

public interface Stage<T, R> {
  Source<R> setup(Source<T> source);

  default <RR> Stage<T, RR> then(Stage<R, RR> next) {
    return (source) -> next.setup(setup(source));
  }

  default <RR> Sink<T, RR> reduce(Sink<R, RR> sink) {
    return (source) -> this.setup(source).reduce(sink);
  }
}
