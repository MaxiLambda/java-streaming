package lincks.maximilian.util;

import java.util.function.*;

public interface Util {
  static <R, T> BiFunction<R, T, R> fluent(BiConsumer<R, T> consumer) {
    return (r, t) -> {
      consumer.accept(r, t);
      return r;
    };
  }

  static <T> Function<T, T> fluent(Consumer<T> consumer) {
    return (t) -> {
      consumer.accept(t);
      return t;
    };
  }

  static <T> T fluent(T t, Consumer<T> consumer) {
    consumer.accept(t);
    return t;
  }

  static Supplier<Void> ignore() {
    return () -> null;
  }

  static <R> R cleanup(Supplier<R> f, Runnable clean) {
    try {
      return f.get();
    } finally {
      clean.run();
    }
  }
}
