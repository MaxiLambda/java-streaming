package lincks.maximilian.streaming;

import java.util.Optional;

public interface Source<T> {
  Optional<T> pull();

  default Source<T> concat(Source<T> other) {
    return () -> {
      Optional<T> token = pull();
      if (token.isEmpty()) {
        return other.pull();
      } else {
        return token;
      }
    };
  }

  default <R> Source<R> then(Stage<T, R> next) {
    return next.setup(this);
  }

  default <RR> RR reduce(Sink<T, RR> sink) {
    return sink.collect(this);
  }
}
