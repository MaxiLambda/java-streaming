package lincks.maximilian.streaming.source;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class IteratorSourceTest {

  @Test
  void iteratorSource() {
    var source = Source.of(1, 2);

    IteratorSource<Integer> iteratorSource = new IteratorSource<>(source);

    assertTrue(iteratorSource.hasNext());
    assertEquals(1, iteratorSource.next());
    assertEquals(2, iteratorSource.next());
    assertFalse(iteratorSource.hasNext());
    assertThrows(NoSuchElementException.class, iteratorSource::next);
  }
}
