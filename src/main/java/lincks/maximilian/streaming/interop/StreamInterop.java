package lincks.maximilian.streaming.interop;

import static java.util.stream.Collectors.toList;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.source.Sources;

public interface StreamInterop {
  static <T> Collector<T, ?, Source<T>> toSourceCollector() {
    return Collectors.collectingAndThen(toList(), Sources::fromIterable);
  }
}
