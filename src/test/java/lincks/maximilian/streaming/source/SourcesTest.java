package lincks.maximilian.streaming.source;

import static lincks.maximilian.streaming.sink.Sinks.foldl;
import static lincks.maximilian.streaming.sink.Sinks.toList;
import static lincks.maximilian.streaming.stage.Stages.take;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import lincks.maximilian.functional.tuple.Tuple;
import org.junit.jupiter.api.Test;

class SourcesTest {

  @Test
  void concat() {
    var source =
        Sources.concat(Source.of(1, 2, 3), Source.empty(), Source.of(4.0, 5.5, 6.7))
            .reduce(toList());

    assertEquals(List.of(1, 2, 3, 4.0, 5.5, 6.7), source);
  }

  @Test
  void fromIterable() {
    var source = Sources.fromIterable(List.of(1, 2, 3)).reduce(toList());

    assertEquals(List.of(1, 2, 3), source);
  }

  @Test
  void fromIterator() {
    var source = Sources.fromIterator(List.of(1, 2, 3).iterator()).reduce(toList());

    assertEquals(List.of(1, 2, 3), source);
  }

  @Test
  void unfold() {
    var res =
        Sources.unfold(
            (num -> num > 0 ? Optional.of(new Tuple<>("!".repeat(num), --num)) : Optional.empty()),
            () -> 3).reduce(toList());

    assertEquals(List.of("!!!", "!!", "!"), res);
  }

  @Test
  void iterate() {
    var res = Sources.iterate(() -> 1, i -> i + 1).then(take(3)).reduce(toList());

    assertEquals(List.of(1, 2, 3), res);
  }

  @Test
  void fetchWhile() {

    var queue = new ArrayDeque<>(List.of(1, 2, 3, 4, 5));

    var res = Sources.fetchWhile(queue::poll, i -> i < 4).reduce(toList());

    assertEquals(List.of(1, 2, 3), res);
  }

  @Test
  void repeat() {
    var res = Sources.repeat(() -> 1, 5).reduce(toList());

    assertEquals(List.of(1, 1, 1, 1, 1), res);
  }

  @Test
  void zip() {
    String s1 = "Hello";
    var ones = Sources.iterate(() -> 5, i -> i - 1);
    var chars = Sources.iterate(() -> 'a', c -> (char) (c + 1)).then(take(5));

    var res = Sources.zip("%d %s"::formatted,ones, chars).reduce(toList());
    assertEquals(List.of("5 a", "4 b", "3 c", "2 d", "1 e"), res);
  }

  @Test
  void cycle() {
    var res = Sources.cycle(Source.of(1, 2, 3)).then(take(10)).reduce(toList());
    assertEquals(List.of(1, 2, 3, 1, 2, 3, 1, 2, 3, 1), res);
    }

  @Test
  void infinite() {
    var res = Sources.infinite(() -> 1).then(take(100_000_000)).reduce(foldl(() -> 0,Integer::sum));

    assertEquals(100_000_000, res);
    }
}
