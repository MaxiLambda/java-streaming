package lincks.maximilian.streaming.stage;

import static lincks.maximilian.streaming.sink.Sinks.toList;
import static lincks.maximilian.streaming.stage.StageChain.$;
import static lincks.maximilian.streaming.stage.Stages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
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

  @Test
  void groupsOf() {
    var res = Source.of(1, 2, 3).then($(Stages.groupsOf(2), mapInner(toList()))).reduce(toList());
    assertEquals(List.of(List.of(1, 2), List.of(3)), res);
  }

  @Test
  void groupsOfExact() {
    var res =
        Source.of(1, 2, 3).then($(Stages.groupsOfExact(2), mapInner(toList()))).reduce(toList());

    assertEquals(List.of(List.of(1, 2)), res);
  }

  @Test
  void take() {
    var res = Source.of(1, 2, 3).then(Stages.take(2)).reduce(toList());
    assertEquals(List.of(1, 2), res);
  }

  @Test
  void takeToBig() {
    var res = Source.of(1, 2, 3).then(Stages.take(7)).reduce(toList());
    assertEquals(List.of(1, 2, 3), res);
  }

  @Test
  void takeWhile() {
    var res = Source.of(1, 2, 3, -4, 5, 6).then(Stages.takeWhile(i -> i > 0)).reduce(toList());

    assertEquals(List.of(1, 2, 3), res);
  }

  @Test
  void takeAll() {
    var res = Source.of(1, 2, 3).then(Stages.takeWhile(_ -> true)).reduce(toList());

    assertEquals(List.of(1, 2, 3), res);
  }

  @Test
  void takeNone() {
    var res = Source.of(1, 2, 3).then(Stages.takeWhile(_ -> false)).reduce(toList());

    assertEquals(List.of(), res);
  }

  @Test
  void dropWhile() {
    var res = Source.of(1, 2, 3, -4, 5, 6).then(Stages.dropWhile(i -> i > 0)).reduce(toList());

    assertEquals(List.of(-4, 5, 6), res);
  }

  @Test
  void dropNone() {
    var res = Source.of(1, 2, 3).then(Stages.dropWhile(i -> i < 0)).reduce(toList());

    assertEquals(List.of(1, 2, 3), res);
  }

  @Test
  void dropAll() {
    var res = Source.of(1, 2, 3).then(Stages.dropWhile(i -> i > 0)).reduce(toList());

    assertEquals(List.of(), res);
  }

  @Test
  void flatMap() {
    var res = Source.of(1, 2, 3).then(Stages.flatMap(i -> Source.of(i, i + 1))).reduce(toList());
    assertEquals(List.of(1, 2, 2, 3, 3, 4), res);
  }

  @Test
  void mapOptional() {
    var res =
        Source.of(1, 20, 3)
            .then(Stages.mapOptional(i -> i % 2 == 0 ? Optional.of("!") : Optional.empty()))
            .reduce(toList());
    assertEquals(List.of("!"), res);
  }

  @Test
  void filterMap() {
    var res =
        Source.of(100, 2000, 30)
            .then(Stages.filterMap(s -> s.length() > 2, String::valueOf))
            .reduce(toList());
    assertEquals(List.of(100, 2000), res);
  }

  @Test
  void scanl() {
    var res = Source.of(1, 2, 3).then(Stages.scanl((s, i) -> s + i, () -> "")).reduce(toList());
    assertEquals(List.of("", "1", "12", "123"), res);
  }

  @Test
  void scanl2() {
    var res = Source.of(1, 2, 3).then(Stages.scanl(Integer::sum)).reduce(toList());
    assertEquals(List.of(1, 3, 6), res);
  }
}
