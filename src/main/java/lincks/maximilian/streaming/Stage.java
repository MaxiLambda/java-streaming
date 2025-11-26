package lincks.maximilian.streaming;

import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import lincks.maximilian.streaming.source.AppendableSourceImpl;

public interface Stage<T, R> {
  Source<R> setup(Source<T> source);

  default <RR> Stage<T, RR> then(Stage<R, RR> next) {
    return (source) -> next.setup(setup(source));
  }

  default <RR> Sink<T, RR> reduce(Sink<R, RR> sink) {
    return (source) -> this.setup(source).reduce(sink);
  }

  default Gatherer<T, ?, R> toGatherer() {
    return Gatherers.fold(AppendableSourceImpl<T>::new, AppendableSourceImpl<T>::append)
        .andThen(
            Gatherer.ofSequential(
                (_, val, dow) -> {
                  setup(val).forEach(dow::push);
                  return false;
                }));
  }
}
