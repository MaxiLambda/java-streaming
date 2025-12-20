package lincks.maximilian.streaming.source;

import static lincks.maximilian.streaming.sink.Sinks.toList;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class SourceTest {

  @Test
  void concat() {
    Source<Integer> source1 = Source.of(1, 2, 3);
    Source<Integer> source2 = Source.of(4, 5, 6);

    List<Integer> result = source1.concat(source2).reduce(toList());

    assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
  }


  @Test
  void concatEmpty() {
    Source<Integer> source1 = Source.of(1, 2, 3);
    Source<Integer> source2 = Source.of(4, 5, 6);

    List<Integer> result = source1.concat(Source.empty()).concat(source2).reduce(toList());

    assertEquals(List.of(1, 2, 3, 4, 5, 6), result);
  }
}
