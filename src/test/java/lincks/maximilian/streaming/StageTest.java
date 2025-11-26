package lincks.maximilian.streaming;

import static lincks.maximilian.streaming.Stages.map;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class StageTest {
  @Test
  void mapGatherer() {
    Stage<Integer, Integer> stage = map(i -> i + 1);
    var list = Stream.of(1, 2, 3).gather(stage.toGatherer()).toList();

    assertTrue(list.contains(2));
    assertTrue(list.contains(3));
    assertTrue(list.contains(4));
  }

  @Test
  void groupsOf2Gatherer() {
    Stage<Integer, List<Integer>> stage = Stages.groupsOf(2);
    var list = Stream.of(1, 2, 3, 4).gather(stage.toGatherer()).toList();
    assertEquals(List.of(List.of(1, 2), List.of(3, 4)), list);
  }
}
