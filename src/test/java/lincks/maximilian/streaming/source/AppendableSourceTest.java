package lincks.maximilian.streaming.source;

import lincks.maximilian.streaming.Source;
import org.junit.jupiter.api.Test;

import java.util.List;

import static lincks.maximilian.streaming.Sinks.list;
import static org.junit.jupiter.api.Assertions.*;

class AppendableSourceTest {
    @Test
    void order(){
        AppendableSourceImpl<Integer> appSource = new AppendableSourceImpl<>();
        appSource.append(1);

        var source =  Source.of(2);

        var appendedSource = appSource.concat(source);
        appendedSource.append(3);

        assertEquals(List.of(1,2,3), appendedSource.reduce(list()));
    }
}
