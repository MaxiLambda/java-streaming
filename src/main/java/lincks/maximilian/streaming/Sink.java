package lincks.maximilian.streaming;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Sink<T, R> {
  R collect(Source<T> source);

  static <T> Sink<T, List<T>> list() {
    return (source) -> {
      ArrayList<T> list = new ArrayList<>();
      while (true) {
        Optional<T> token = source.pull();
        if (token.isEmpty()) {
          return list;
        } else {
          list.add(token.get());
        }
      }
    };
  }
}
