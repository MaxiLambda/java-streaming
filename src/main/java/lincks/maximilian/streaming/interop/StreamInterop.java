package lincks.maximilian.streaming.interop;

import static java.util.stream.Collectors.toList;
import static lincks.maximilian.streaming.source.Sources.fromIterable;
import static lincks.maximilian.streaming.stage.Stages.buffer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
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

  static <T, A, R> Stage<T, R> fromGatherer(Gatherer<T, A, R> gatherer) {
    Gatherer.Integrator<A, T, R> integrator = gatherer.integrator();
    Supplier<A> initializer = gatherer.initializer();
    BiConsumer<A, Gatherer.Downstream<? super R>> finisher = gatherer.finisher();

    // array buffer to act as downstream
    ArrayList<R> listBuffer = new ArrayList<>();

    Supplier<Optional<Source<R>>> createOptionalAndClearBuffer =
        () -> {
          try {
            // we need to wrap the buffer in a new List to prevent issues on mutations
            return Optional.of(fromIterable(new ArrayList<>(listBuffer)));
          } finally {
            listBuffer.clear();
          }
        };

    Stage<T, Source<R>> intermediate =
        (source -> {
          // initialization must happen here to persist the state between pushes
          // don't call get() if the initializer is the default value
          A state = Gatherer.defaultInitializer().equals(initializer) ? null : initializer.get();
          // this external counter is required, otherwise the finisher might be called an infinite
          // number of times
          AtomicBoolean finished = new AtomicBoolean(false);
          return () -> {
            // listBuffer only contains values if the finisher called downstream.push(...)
            while (listBuffer.isEmpty()) {
              Optional<T> token = source.pull();
              if (token.isEmpty()) {
                // check if a finisher exists
                if (finished.get() || Gatherer.defaultFinisher().equals(finisher)) {
                  // no finisher, nothing to push downstream
                  return Optional.empty();
                } else {
                  finished.set(true);
                  finisher.accept(state, listBuffer::add);
                  // the finisher can call downstream.push(...), therefore check if new elements
                  // exist

                  return listBuffer.isEmpty()
                      ? Optional.empty()
                      : createOptionalAndClearBuffer.get();
                }
              }
              integrator.integrate(state, token.get(), listBuffer::add);
            }
            return createOptionalAndClearBuffer.get();
          };
        });

    return intermediate.then(buffer());
  }
}
