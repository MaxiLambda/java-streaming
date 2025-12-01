package lincks.maximilian.streaming.source;

import static lincks.maximilian.streaming.stage.Stages.map;
import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class SourceTest {
  @Test
  void stream() {
    var list = Source.of(1, 2).then(map(i -> i + 1)).toStream().toList();

    assertTrue(list.contains(2));
    assertTrue(list.contains(3));
  }
}
