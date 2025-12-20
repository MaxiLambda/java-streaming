package lincks.maximilian.streaming.source;

import org.junit.jupiter.api.Test;

import java.util.List;

import static lincks.maximilian.streaming.sink.Sinks.toList;
import static org.junit.jupiter.api.Assertions.*;

class SourcesTest {


    @Test
    void concat() {
        var source = Sources.concat(Source.of(1,2,3), Source.empty(), Source.of(4.0,5.5,6.7)).reduce(toList());

        assertEquals(List.of(1,2,3,4.0,5.5,6.7), source);
    }
}
