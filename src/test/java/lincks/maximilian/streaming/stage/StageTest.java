package lincks.maximilian.streaming.stage;

import static lincks.maximilian.streaming.interop.StreamInterop.toTerminalGatherer;
import static lincks.maximilian.streaming.sink.Sinks.toList;
import static lincks.maximilian.streaming.stage.Stages.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StageTest {
  @Test
  void toGatherer() {
    var res = Stream.of(1, 2, 3).gather(toTerminalGatherer(map((Integer i) -> i + 1))).toList();

    assertEquals(List.of(2, 3, 4), res);
  }

  @Test
  void toGatherer2() {
    var res = Stream.of(1, 2, 3).gather(toTerminalGatherer(limit(1))).toList();

    assertEquals(List.of(1), res);
  }

  @Test
  void toGatherer3() {
    var res =
        Stream.of(1, 2, 3)
            .gather(toTerminalGatherer(slidingWindow(2).then(mapInner(toList()))))
            .toList();

    assertEquals(List.of(List.of(1, 2), List.of(2, 3)), res);
  }
}
