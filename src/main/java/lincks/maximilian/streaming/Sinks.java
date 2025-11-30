package lincks.maximilian.streaming;

import static lincks.maximilian.streaming.Util.fluent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface Sinks {

  static <T> Sink<T, List<T>> toList() {
    return foldl(new ArrayList<>(), fluent(List::add));
  }
//  TODO
//  static <T,K,V> Sink<T, Map<K,V>> toMap() {
//
//  }

  static <T, R> Sink<T, R> foldl(R identity, BiFunction<R, T, R> accumulator) {
    return (source) -> {
      R acc = identity;
      while (true) {
        Optional<T> token = source.pull();
        if (token.isEmpty()) {
          return acc;
        } else {
          acc = accumulator.apply(acc, token.get());
        }
      }
    };
  }

  static <T, R> Sink<T, R> foldl(Supplier<R> identity, BiFunction<R, T, R> accumulator) {
    return (source) -> {
      R acc = identity.get();
      while (true) {
        Optional<T> token = source.pull();
        if (token.isEmpty()) {
          return acc;
        } else {
          acc = accumulator.apply(acc, token.get());
        }
      }
    };
  }

  static <T> Sink<T, Optional<T>> foldl(BiFunction<T, T, T> accumulator) {
    return (source) -> {
      Optional<T> acc = Optional.empty();
      while (true) {
        Optional<T> token = source.pull();
        if (token.isEmpty()) {
          return acc;
        } else {
          acc = acc.flatMap(a -> token.map(t -> accumulator.apply(a, t)));
        }
      }
    };
  }
}
