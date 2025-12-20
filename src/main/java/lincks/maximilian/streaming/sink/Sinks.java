package lincks.maximilian.streaming.sink;

import static lincks.maximilian.util.Util.fluent;

import java.util.*;
import java.util.function.*;

public interface Sinks {

  static <T> Sink<T, List<T>> toList() {
    return intoMutable(ArrayList::new, List::add);
  }

  static <T, K, V> Sink<T, Map<K, V>> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper) {
    return intoMutable(HashMap::new, (map, t) -> map.put(keyMapper.apply(t), valueMapper.apply(t)));
  }

  static <C, T> Sink<T, C> into(Supplier<C> createContainer, BiFunction<C, T, C> conj) {
    return foldl(createContainer, conj);
  }

  static <C, T> Sink<T, C> intoMutable(Supplier<C> createContainer, BiConsumer<C, T> conj) {
    return foldl(createContainer, fluent(conj));
  }

  static <T, K, V> Sink<T, Map<K, List<V>>> toMultiMap(
      Function<T, K> keyMapper, Function<T, V> valueMapper) {
    return intoMutable(
        HashMap::new,
        (map, t) ->
            map.merge(
                keyMapper.apply(t), valueMapper.andThen(List::of).apply(t), fluent(List::addAll)));
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
    return (source) ->
        foldl(source::pull, (Optional<T> acc, T val) -> acc.map(a -> accumulator.apply(a, val)))
            .collect(source);
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
          acc = acc.compose((R val) -> accumulator.apply(token.get(), val));
        }
      }
    };
  }

  static <T> Sink<T, Optional<T>> foldr(BiFunction<T, T, T> accumulator) {
    return (source) ->
        foldr(source::pull, (T val, Optional<T> acc) -> acc.map(a -> accumulator.apply(val, a)))
            .collect(source);
  }

  static <T> Sink<T, Void> forEach(Consumer<T> action) {
    return (source) -> {
      while (true) {
        Optional<T> token = source.pull();
        if (token.isEmpty()) {
          return null;
        } else {
          action.accept(token.get());
        }
      }
    };
  }
}
