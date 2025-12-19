package lincks.maximilian.streaming.interop;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static lincks.maximilian.streaming.sink.Sinks.toList;
import static org.junit.jupiter.api.Assertions.*;

class StreamInteropTest {

    @Test
    void collector() {
        var list = Stream.of(1, 2, 3).collect(StreamInterop.toCollector(toList()));
        assertEquals(List.of(1, 2, 3), list);
    }

}
