package lincks.maximilian.streaming.interop;

import static java.util.stream.Collectors.toList;
import static lincks.maximilian.streaming.source.Sources.fromIterable;
import static lincks.maximilian.streaming.stage.Stages.buffer;
import static lincks.maximilian.streaming.stage.Stages.integrate;
import static lincks.maximilian.util.Util.cleanup;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.*;
import lincks.maximilian.streaming.sink.Sink;
import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.source.Sources;
import lincks.maximilian.streaming.source.SpliteratorSource;
import lincks.maximilian.streaming.stage.Stage;
import lincks.maximilian.streaming.stage.Stages;

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
    return StreamSupport.stream(new SpliteratorSource<>(source), false);
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

  static <T, A, R> Stage<T, R> fromGatherer(Gatherer<T, A, R> gatherer) {
    Gatherer.Integrator<A, T, R> integrator = gatherer.integrator();
    Supplier<A> initializer = gatherer.initializer();
    BiConsumer<A, Gatherer.Downstream<? super R>> finisher = gatherer.finisher();

    // array buffer to act as downstream
    ArrayList<R> listBuffer = new ArrayList<>();

    Supplier<Optional<Source<R>>> listBufferToSource =
        () -> Optional.of(fromIterable(new ArrayList<>(listBuffer)));

    Stage<T, Source<R>> intermediate =
        integrate(
            () -> Gatherer.defaultInitializer().equals(initializer) ? null : initializer.get(),
            (val, acc) -> {
              // acc may be mutated here
              integrator.integrate(acc, val, listBuffer::add);
              return Stages.State.of(acc, cleanup(listBufferToSource, listBuffer::clear));
            },
            acc -> {
              finisher.accept(acc, listBuffer::add);
              return listBuffer.isEmpty()
                  ? Optional.empty()
                  : cleanup(listBufferToSource, listBuffer::clear);
            });

    return intermediate.then(buffer());
  }
}
