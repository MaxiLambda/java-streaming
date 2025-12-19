package lincks.maximilian.streaming.source;

import static lincks.maximilian.streaming.interop.StreamInterop.toStream;
import static lincks.maximilian.streaming.stage.Stages.map;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SourceTest {
  @Test
  void stream() {
    var s = Source.of(1, 2).then(map(i -> i + 1));
    var list = toStream(s).toList();

    assertTrue(list.contains(2));
    assertTrue(list.contains(3));
  }
}
