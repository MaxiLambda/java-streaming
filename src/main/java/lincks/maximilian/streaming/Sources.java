package lincks.maximilian.streaming;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import lincks.maximilian.streaming.source.AppendableSource;
import lincks.maximilian.streaming.source.AppendableSourceImpl;

import static java.util.stream.Collectors.toList;

public interface Sources {

  static <T> Collector<T, ?, Source<T>> sourceCollector() {
    return Collectors.collectingAndThen(toList(), Source::fromIterable);
  }
}
