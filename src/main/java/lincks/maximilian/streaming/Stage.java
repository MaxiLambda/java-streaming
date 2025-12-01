package lincks.maximilian.streaming;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;

public interface Stage<T, R> {
  Source<R> setup(Source<T> source);

  default <RR> Stage<T, RR> then(Stage<R, RR> next) {
    return (source) -> next.setup(setup(source));
  }

  default <RR> Sink<T, RR> reduce(Sink<R, RR> sink) {
    return (source) -> this.setup(source).reduce(sink);
  }

  default Gatherer<T, ?, R> toTerminalGatherer() {
    // use a fold to gather all elements in a list
    return Gatherers.<T, List<T>>fold(
            ArrayList::new,
            (acc, val) -> {
              acc.add(val);
              return acc;
            })
        .andThen(
            Gatherer.ofSequential(
                (_, list, downstream) -> {
                  //                  turn the list into a source and feed it to this stage
                  setup(Source.fromIterable(list))
                      // push all values created by this stage downstream
                      .forEach(downstream::push);
                  return false;
                }));
  }
}
