package lincks.maximilian.streaming.source;

import java.util.Iterator;
import java.util.Optional;

public interface Sources {

  /** Creates a new Source based on the given Iterable. */
  static <T> Source<T> fromIterable(Iterable<T> iterable) {
    return fromIterator(iterable.iterator());
  }

  /** Creates a new Source based on the given Iterator. */
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
