package lincks.maximilian;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
  public static void main(String[] args) {

    var source = new CollectionSource<>(List.of(1, 2, 3));
    var mapS = new MapStage<>(source, i -> i + 1);
    var filterS = new LimitStage<>(mapS, 0);
    var sink = new ListSink<>(filterS);

    System.out.println(sink.collect());
    // Source: pull
    // Stage: pull, buffer?
    // Sink:

  }

  sealed interface Token<T> {
    default boolean isEOS() {
      return this instanceof EOF;
    }

    default T getValue() {
      throw new UnsupportedOperationException();
    }
  }

  // EndOfFlow -> EOF
  record EOF<T>() implements Token<T> {}

  record Value<T>(T value) implements Token<T> {
    @Override
    public T getValue() {
      return value();
    }
  }

  interface Source<T> {
    Token<T> pull();

    default Source<T> concat(Source<T> other) {
      return () -> {
        Token<T> token = pull();
        if (token.isEOS()) {
          return other.pull();
        } else {
          return token;
        }
      };
    }
  }

  interface Stage<T, R> extends Source<R> {

  }

  interface Sink<T, R> {
    R collect();
  }

  static class CollectionSource<T> implements Source<T> {
    private final Iterator<T> collection;

    public CollectionSource(Collection<T> collection) {
      this.collection = collection.iterator();
    }

    @Override
    public Token<T> pull() {
      if (collection.hasNext()) {
        return new Value<>(collection.next());
      } else {
        return new EOF<>();
      }
    }
  }

  record MapStage<T, R>(Source<T> source, Function<T, R> transformer) implements Stage<T, R> {

    @Override
    public Token<R> pull() {
      Token<T> token = source.pull();
      if (token.isEOS()) {
        return new EOF<>();
      } else {
        return new Value<>(transformer.apply(token.getValue()));
      }
    }
  }

  static class LimitStage<T> implements Stage<T, T> {

    private final Source<T> source;
    private final int limit;
    private int counter = 0;

    public LimitStage(Source<T> source, int limit) {
      this.source = source;
      this.limit = limit;
    }

    @Override
    public Token<T> pull() {
      if (limit < ++counter) {
        return new EOF<>();
      } else {
        return source.pull();
      }
    }
  }

  static class BufferStage<T> implements Stage<Source<T>,T> {

    private final Source<Source<T>> source;
    private Source<T> bufferedSource = EOF::new;

      BufferStage(Source<Source<T>> source) {
          this.source = source;
      }

      @Override
    public Token<T> pull() {
      Token<T> token = bufferedSource.pull();
      if (token.isEOS()) {
        Token<Source<T>> sourceToken = source.pull();
        if(sourceToken.isEOS()) {
          return new EOF<>();
        } else {
          bufferedSource = sourceToken.getValue();
          return pull();
        }
      } else {
       return token;
      }
    }
  }

  record ListSink<T>(Source<T> source) implements Sink<T, List<T>> {
    @Override
    public List<T> collect() {
      ArrayList<T> list = new ArrayList<>();
      while (true) {
        Token<T> token = source.pull();
        if (token.isEOS()) {
          return list;
        } else {
          list.add(token.getValue());
        }
      }
    }
  }
}
