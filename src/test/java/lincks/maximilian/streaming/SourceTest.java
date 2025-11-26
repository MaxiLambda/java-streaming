package lincks.maximilian.streaming;

import static lincks.maximilian.streaming.Stages.map;
import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class SourceTest {

  @Test
  void iteratorSource() {
    var source = Source.of(1, 2);

    Source.IteratorSource<Integer> iteratorSource = new Source.IteratorSource<>(source);

    assertTrue(iteratorSource.hasNext());
    assertEquals(1, iteratorSource.next());
    assertEquals(2, iteratorSource.next());
    assertFalse(iteratorSource.hasNext());
    assertThrows(NoSuchElementException.class, iteratorSource::next);
  }

  @Test
  void stream() {
    var list = Source.of(1, 2).then(map(i -> i + 1)).toStream().toList();

    assertTrue(list.contains(2));
    assertTrue(list.contains(3));
  }
}
