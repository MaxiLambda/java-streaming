package lincks.maximilian.streaming.interop;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.*;
import lincks.maximilian.streaming.sink.Sink;
import lincks.maximilian.streaming.source.IteratorSource;
import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.source.Sources;
import lincks.maximilian.streaming.stage.Stage;

public interface StreamInterop {
  static <T> Collector<T, ?, Source<T>> toSourceCollector() {
    return Collectors.collectingAndThen(toList(), Sources::fromIterable);
  }

  static <T, R> Sink<T, R> fromCollector(Collector<T, ?, R> collector) {
    return (source) -> toStream(source).collect(collector);
  }

  /** Creates a {@link Collector} from this Sink. */
  static <T, R> Collector<T, ?, R> toCollector(Sink<T, R> sink) {
    return Collectors.collectingAndThen(toSourceCollector(), sink::collect);
  }

  /** Converts this Source into a sequential ordered {@link Stream}. */
  static <T> Stream<T> toStream(Source<T> source) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(new IteratorSource<>(source), Spliterator.ORDERED),
        false);
  }

  /**
   * Turns this Stage into a sequential {@link Gatherer}.
   *
   * @return Gatherer which reads the while Stream, turns it into a {@link Source}, processes all
   *     values and finally pushes all new values downstream.
   */
  static <T, R> Gatherer<T, ?, R> toTerminalGatherer(Stage<T, R> stage) {
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
                  stage
                      .setup(Sources.fromIterable(list))
                      // push all values created by this stage downstream
                      .forEach(downstream::push);
                  return false;
                }));
  }
}
