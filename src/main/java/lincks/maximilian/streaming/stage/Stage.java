package lincks.maximilian.streaming.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
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
  }

  /**
   * Turns this Stage into a sequential {@link Gatherer}.
   *
   * @return Gatherer which reads the while Stream, turns it into a {@link Source}, processes all
   *     values and finally pushes all new values downstream.
   */
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
