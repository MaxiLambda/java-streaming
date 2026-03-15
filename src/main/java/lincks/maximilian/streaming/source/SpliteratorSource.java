package lincks.maximilian.streaming.source;

import java.util.Spliterator;
import java.util.function.Consumer;

public class SpliteratorSource<T> implements Spliterator<T> {

  private final Source<T> source;

  public SpliteratorSource(Source<T> source) {
    this.source = source;
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    return source
        .pull()
        .map(
            e -> {
              action.accept(e);
              return true;
            })
        .orElse(false);
  }

  @Override
  public Spliterator<T> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return ORDERED;
  }
}
