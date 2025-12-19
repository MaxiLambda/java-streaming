package lincks.maximilian.streaming.sink;

import static lincks.maximilian.streaming.sink.Sinks.toList;
import static lincks.maximilian.streaming.stage.Stages.mapInner;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import lincks.maximilian.streaming.source.Source;
import org.junit.jupiter.api.Test;

class SinkTest {

  @Test
  void useAsStage() {
    Source<Source<String>> source = Source.of(Source.of("a", "b"), Source.of(), Source.of("c"));

    var result = source.then(mapInner(toList())).reduce(toList());
    assertEquals(List.of(List.of("a", "b"), List.of(), List.of("c")), result);
  }
}
