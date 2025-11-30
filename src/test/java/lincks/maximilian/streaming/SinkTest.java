package lincks.maximilian.streaming;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SinkTest {

  @Test
  void collector() {
    var list = Stream.of(1, 2, 3).collect(Sinks.toList().toCollector());
    assertEquals(List.of(1, 2, 3), list);
  }
}
