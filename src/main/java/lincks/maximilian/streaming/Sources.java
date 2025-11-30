package lincks.maximilian.streaming;

import static java.util.stream.Collectors.toList;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public interface Sources {

  static <T> Collector<T, ?, Source<T>> toSourceCollector() {
    return Collectors.collectingAndThen(toList(), Source::fromIterable);
  }
}
