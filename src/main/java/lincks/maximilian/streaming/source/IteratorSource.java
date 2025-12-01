package lincks.maximilian.streaming.source;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/** Turns a {@link Source} into a {@link Iterator} by adding buffering. */
public class IteratorSource<T> implements Iterator<T> {

  private final Source<T> source;
  private Optional<T> bufferedValue = Optional.empty();

  /**
   * Create a new {@link Iterator} from the given {@link Source}.
   */
  public IteratorSource(Source<T> source) {
    this.source = source;
  }

  @Override
  public boolean hasNext() {
    if (bufferedValue.isPresent()) return true;
    bufferedValue = source.pull();
    return bufferedValue.isPresent();
  }

  @Override
  public T next() {
    if (hasNext()) {
      try {
        // hasNext() always pulls the next value
        // it can only return true if bufferedValue.isPresent() is true
        return bufferedValue.orElseThrow();
      } finally {
        bufferedValue = Optional.empty();
      }
    }
    throw new NoSuchElementException();
  }
}
