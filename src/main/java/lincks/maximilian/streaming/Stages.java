package lincks.maximilian.streaming;

import static lincks.maximilian.streaming.Sources.fromIterable;

import java.util.*;
import java.util.function.Function;

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
          //create a copy of the queue in a collection because the queue is mutated
          var ret = fromIterable(new ArrayList<>(queue));
          queue.removeFirst();
          return Optional.of(ret);
        };
  }
}
