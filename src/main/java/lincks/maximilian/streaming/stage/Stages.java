package lincks.maximilian.streaming.stage;

import lincks.maximilian.streaming.source.Source;

import static lincks.maximilian.streaming.source.Source.fromIterable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

public interface Stages {
  static <T> Stage<T, T> identity() {
    return source -> source;
  }

  static <T, R> Stage<T, R> map(Function<T, R> transformer) {
    return source -> () -> source.pull().map(transformer);
  }

  static <T, R> Stage<T, R> flatMap(Function<T, Source<R>> transformer) {
    return map(transformer).then(buffer());
  }

  static <T> Stage<T, T> limit(int limit) {
    return new Stage<>() {
      private int counter = 0;

      @Override
      public Source<T> setup(Source<T> source) {
        return () -> {
          if (limit < ++counter) {
            return Optional.empty();
          } else {
            return source.pull();
          }
        };
      }
    };
  }

  static <T> Stage<Source<T>, T> buffer() {
    return source ->
        new Source<>() {
          private Source<T> bufferedSource = Optional::empty;

          @Override
          public Optional<T> pull() {
            {
              Optional<T> token = bufferedSource.pull();
              if (token.isEmpty()) {
                Optional<Source<T>> sourceToken = source.pull();
                if (sourceToken.isEmpty()) {
                  return Optional.empty();
                } else {
                  bufferedSource = sourceToken.get();
                  return pull();
                }
              } else {
                return token;
              }
            }
          }
        };
  }

  /**
   * Split the Source into sequences of length size. If not enough values are present, the last
   * group may be smaller than size.
   */
  static <T> Stage<T, List<T>> groupsOf(int size) {
    return (source) ->
        () -> {
          ArrayList<T> list = new ArrayList<>();
          while (list.size() < size) {
            Optional<T> token = source.pull();
            if (token.isEmpty()) {
              if (list.isEmpty()) {
                return Optional.empty();
              }
              return Optional.of(list);
            } else {
              list.add(token.get());
            }
          }
          return Optional.of(list);
        };
  }

  static <T> Stage<T, Source<T>> slidingWindow(int size) {
    Deque<T> queue = new ArrayDeque<>();
    return (source) ->
        () -> {
          // take elements until the que is full
          while (queue.size() < size) {
            Optional<T> token = source.pull();
            if (token.isEmpty()) {
              return Optional.empty();
            } else {
              queue.addLast(token.get());
            }
          }
          // create a copy of the queue in a collection because the queue is mutated
          var ret = fromIterable(new ArrayList<>(queue));
          queue.removeFirst();
          return Optional.of(ret);
        };
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
                        return Optional.of(Source.fromIterable(new ArrayList<>(listBuffer)));
                    } finally {
                        listBuffer.clear();
                    }
                };

        Stage<T, Source<R>> intermediate =
                (source ->
                        () -> {
                            // don't call get() if the initializer is the default value
                            A state =
                                    Gatherer.defaultInitializer().equals(initializer) ? null : initializer.get();
                            // listBuffer only contains values if the finisher called downstream.push(...)
                            while (listBuffer.isEmpty()) {
                                Optional<T> token = source.pull();
                                if (token.isEmpty()) {
                                    // check if a finisher exists
                                    if (Gatherer.defaultFinisher().equals(finisher)) {
                                        // no finisher, nothing to push downstream
                                        return Optional.empty();
                                    } else {
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
                        });

        return intermediate.then(buffer());
    }

    record State<A, R>(A acc, Optional<R> result) {}

    static <T, A, R> Stage<T, R> integrate(
            Supplier<A> initializer, BiFunction<T, A, State<A, R>> accumulator) {
        return source ->
                () -> {
                    State<A, R> state = new State<>(initializer.get(), Optional.empty());
                    while (state.result.isEmpty()) {
                        Optional<T> token = source.pull();
                        if (token.isEmpty()) {
                            // state contains no valid result, but there is no data
                            return Optional.empty();
                        }
                        state = accumulator.apply(token.get(), state.acc);
                    }
                    return state.result;
                };
    }
}
