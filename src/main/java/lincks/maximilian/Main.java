package lincks.maximilian;

import static lincks.maximilian.streaming.sink.Sinks.*;
import static lincks.maximilian.streaming.source.Sources.source;
import static lincks.maximilian.streaming.stage.Stages.*;

import java.util.List;
import java.util.function.BiFunction;
import lincks.maximilian.streaming.source.never.Never;
import lincks.maximilian.streaming.source.Source;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
  public static void main(String[] args) {
//
//    BiFunction<String, Character, String> concatFlipped = (String str, Character chr) -> chr + str;
//    BiFunction<Character, String, String> concat = (Character chr, String str) -> chr + str;
//
//    System.out.println(Source.of('a', 'b', 'c').reduce(foldl(() -> "!", concatFlipped)));
//    System.out.println(Source.of('a', 'b', 'c').reduce(foldr(() -> "!", concat)));
//
//    System.out.println(Source.of(1, 2, 3).reduce(foldl(Integer::sum)));
//
//    List<List<Integer>> res =
//        Source.of(1, 2, 3)
//            .then(
//                $(
//                    flatMap((i) -> Source.of(i, i + 1)),
//                    map(i -> i + 1),
//                    s -> source(() -> s.pull().map(i -> i + 1)),
//                    map(i -> i + 1),
//                    slidingWindow(4),
//                    map(s -> s.then(map(i -> i + 1))),
//                    mapInner(toList())))
//            .reduce(toList());
//
//    System.out.println(res);
//
//    // void stuff
//    var stages = $(Never.asNeverStage(Source.of("hi", "bye")), map(String::toUpperCase));
//    var voidExperiments = Never.fromNever(stages).reduce(toList());
//
//    System.out.println(voidExperiments);

    Source.of(1, 2, 3, 4).then(limit(2)).forEach(System.out::println);
  }
}
