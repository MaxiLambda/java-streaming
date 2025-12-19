package lincks.maximilian.streaming.stage;

import static lincks.maximilian.streaming.sink.Sinks.toList;
import static lincks.maximilian.streaming.stage.Stages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import lincks.maximilian.streaming.source.Source;
import org.junit.jupiter.api.Test;

class StagesTest {

  @Test
  void filter() {
    var res = Source.of(1, 2, 3).then(Stages.filter(i -> i % 2 == 0)).reduce(toList());
    assertEquals(List.of(2), res);
  }

  @Test
  void slidingWindow() {
    var res =
        Source.of(1, 2, 3).then($(Stages.slidingWindow(2), mapInner(toList()))).reduce(toList());
    System.out.println(res);
  }
}
