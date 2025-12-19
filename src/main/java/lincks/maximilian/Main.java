package lincks.maximilian;

import static lincks.maximilian.streaming.sink.Sinks.*;
import static lincks.maximilian.streaming.stage.Stages.*;

import java.util.function.BiFunction;
import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.stage.Stage;
import lincks.maximilian.streaming.stage.Stages;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
  public static void main(String[] args) {

    BiFunction<String, Character, String> concatFlipped = (String str, Character chr) -> chr + str;
    BiFunction<Character, String, String> concat = (Character chr, String str) -> chr + str;

    System.out.println(Source.of('a', 'b', 'c').reduce(foldl(() -> "!", concatFlipped)));
    System.out.println(Source.of('a', 'b', 'c').reduce(foldr(() -> "!", concat)));

    System.out.println(Source.of(1,2,3).reduce(foldl(Integer::sum)));

    Stage<Integer, Source<Integer>> slidingWindow = slidingWindow(4);

    var xxx = slidingWindow.then(mapInner(toList()));

    var res =
        Source.of(1, 2, 3)
            .then(
                Stages.<Integer, Integer>flatMap(i -> Source.of(i, i + 1))
                    .then(slidingWindow(4))
                    .then(mapInner(toList())))
            .reduce(toList());

    Source.of(1, 2, 3)
        .then(flatMap(i -> Source.of(i, i + 1)))
        .then(slidingWindow(4))
        .then(mapInner(toList()))
        .reduce(toList());

    System.out.println(res);
  }
}
