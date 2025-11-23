package lincks.maximilian.streaming;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public interface Stage<T, R> {
  Source<R> setup(Source<T> source);

  default <RR> Stage<T, RR> then(Stage<R, RR> next) {
    return (source) -> next.setup(setup(source));
  }

  default <RR> Sink<T, RR> reduce(Sink<R, RR> sink) {
    return (source) -> this.setup(source).reduce(sink);
  }

  static <T, R> Stage<T, R> map(Function<T, R> transformer) {
    return source ->
        () -> {
          Optional<T> token = source.pull();
          return token.map(transformer);
        };
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
        new Source<T>() {
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
}
