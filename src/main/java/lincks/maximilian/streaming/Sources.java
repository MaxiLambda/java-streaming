package lincks.maximilian.streaming;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

public interface Sources {
  static <T> Source<T> empty() {
    return Optional::empty;
  }

  static <T> Source<T> of(T... elements) {
    return fromIterable(Arrays.asList(elements));
  }

  static <T> Source<T> fromIterable(Iterable<T> iterable) {
    return fromIterator(iterable.iterator());
  }

  static <T> Source<T> fromIterator(Iterator<T> iterator) {
    return () -> {
      if (iterator.hasNext()) {
        return Optional.of(iterator.next());
      } else {
        return Optional.empty();
      }
    };
  }
}
