package lincks.maximilian.streaming.sink;

import static lincks.maximilian.util.Util.fluent;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Sinks {

  static <T> Sink<T, List<T>> toList() {
    return foldl(ArrayList::new, fluent(List::add));
  }

  static <T, K, V> Sink<T, Map<K, V>> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper) {
    return foldl(
        HashMap::new, fluent((map, t) -> map.put(keyMapper.apply(t), valueMapper.apply(t))));
  }

  static <T, K, V> Sink<T, Map<K, List<V>>> toMultiMap(
      Function<T, K> keyMapper, Function<T, V> valueMapper) {
    return foldl(
        HashMap::new,
        fluent(
            (map, t) ->
                map.merge(
                    keyMapper.apply(t),
                    valueMapper.andThen(List::of).apply(t),
                    fluent(List::addAll))));
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

  // foldl' :: (a -> b -> a) -> a -> [b] -> a
  // foldl' _ acc [] = acc
  // foldl' f acc (x:xs) = foldl' f (f acc x) xs
  // cba!
  //
  // foldr' :: (b -> a -> a) -> a -> [b] -> a
  // foldr' _ acc [] = acc
  // foldr' f acc (x:xs) = f x (foldr' f acc xs)
  // abc!
  static <T, R> Sink<T, R> foldr(Supplier<R> identity, BiFunction<T, R, R> accumulator) {
    return (source) -> {
      Function<R, R> acc = Function.identity();
      while (true) {
        Optional<T> token = source.pull();
        if (token.isEmpty()) {
          return acc.apply(identity.get());
        } else {
          acc =  acc.compose((R val) -> accumulator.apply(token.get(), val));
        }
      }
    };
  }
}
