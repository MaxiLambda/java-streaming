package lincks.maximilian;

import static lincks.maximilian.streaming.Sink.list;
import static lincks.maximilian.streaming.Stage.*;

import lincks.maximilian.streaming.Source;
import lincks.maximilian.streaming.Stage;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
  public static void main(String[] args) {

    var res =
        Source.of(1, 2, 3)
            .then(map(i -> i + 1))
            .then(limit(2))
            .then(flatMap(i -> Source.of(i, i + 1)))
            .reduce(list());

    System.out.println(res);

    var sink =
        Stage.<Integer, Integer>map(i -> i + 1)
            .then(limit(2))
            .then(flatMap(i -> Source.of(i, i + 1)))
            .reduce(list());

    System.out.println(Source.of(1, 2, 3).reduce(sink));
  }
}
