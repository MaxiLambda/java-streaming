package lincks.maximilian.streaming.stage;

import static lincks.maximilian.streaming.sink.Sinks.toList;
import static lincks.maximilian.streaming.stage.Stages.map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import lincks.maximilian.streaming.source.Source;
import org.junit.jupiter.api.Test;

class StagesTest {
  @Test
  void mapGatherer() {
    Stage<Integer, Integer> stage = map(i -> i + 1);
    var list = Stream.of(1, 2, 3).gather(stage.toTerminalGatherer()).toList();

    assertTrue(list.contains(2));
    assertTrue(list.contains(3));
    assertTrue(list.contains(4));
  }

  @Test
  void groupsOf2Gatherer() {
    Stage<Integer, List<Integer>> stage = Stages.groupsOf(2);
    var list = Stream.of(1, 2, 3, 4).gather(stage.toTerminalGatherer()).toList();
    assertEquals(List.of(List.of(1, 2), List.of(3, 4)), list);
  }

  @Test
  void filter() {
    var res = Source.of(1,2,3).then(Stages.filter(i -> i % 2 == 0)).reduce(toList());
    assertEquals(List.of(2), res);
  }

  @Test
  void fromGatherer() {
    var res = Source.of(1,2,3).then(Stages.fromGatherer(Gatherers.windowSliding(2))).reduce(toList());

    assertEquals(List.of(List.of(1,2), List.of(2,3)), res);
  }

  @Test
  void fromGatherer2() {
    var res = Source.of(1,2,3).then(Stages.fromGatherer(Gatherers.fold(() -> 0, Integer::sum))).reduce(toList());
    assertEquals(List.of(6), res);
  }

  @Test
  void fromGatherer3() {
    var res = Source.of(1,2,3).then(Stages.fromGatherer(Gatherers.scan(() -> 0, Integer::sum))).reduce(toList());
    assertEquals(List.of(1,3,6), res);
  }
}
