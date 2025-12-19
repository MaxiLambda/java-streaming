package lincks.maximilian.streaming.interop;

import static lincks.maximilian.streaming.interop.StreamInterop.toStream;
import static lincks.maximilian.streaming.interop.StreamInterop.toTerminalGatherer;
import static lincks.maximilian.streaming.sink.Sinks.toList;
import static lincks.maximilian.streaming.stage.Stages.*;
import static lincks.maximilian.streaming.stage.Stages.mapInner;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.stage.Stage;
import lincks.maximilian.streaming.stage.Stages;
import org.junit.jupiter.api.Test;

class StreamInteropTest {

  @Test
  void collector() {
    var list = Stream.of(1, 2, 3).collect(StreamInterop.toCollector(toList()));
    assertEquals(List.of(1, 2, 3), list);
  }

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

  @Test
  void fromGatherer() {
    var res =
        Source.of(1, 2, 3)
            .then(StreamInterop.fromGatherer(Gatherers.windowSliding(2)))
            .reduce(toList());

    assertEquals(List.of(List.of(1, 2), List.of(2, 3)), res);
  }

  @Test
  void fromGatherer2() {
    var res =
        Source.of(1, 2, 3)
            .then(StreamInterop.fromGatherer(Gatherers.fold(() -> 0, Integer::sum)))
            .reduce(toList());
    assertEquals(List.of(6), res);
  }

  @Test
  void fromGatherer3() {
    var res =
        Source.of(1, 2, 3)
            .then(StreamInterop.fromGatherer(Gatherers.scan(() -> 0, Integer::sum)))
            .reduce(toList());
    assertEquals(List.of(1, 3, 6), res);
  }

  @Test
  void stream() {
    var s = Source.of(1, 2).then(map(i -> i + 1));
    var list = toStream(s).toList();

    assertTrue(list.contains(2));
    assertTrue(list.contains(3));
  }

  @Test
  void mapGatherer() {
    Stage<Integer, Integer> stage = map(i -> i + 1);
    var list = Stream.of(1, 2, 3).gather(toTerminalGatherer(stage)).toList();

    assertTrue(list.contains(2));
    assertTrue(list.contains(3));
    assertTrue(list.contains(4));
  }

  @Test
  void groupsOf2Gatherer() {
    Stage<Integer, List<Integer>> stage = $(Stages.groupsOf(2), mapInner(toList()));
    var list = Stream.of(1, 2, 3, 4).gather(toTerminalGatherer(stage)).toList();
    assertEquals(List.of(List.of(1, 2), List.of(3, 4)), list);
  }
}
