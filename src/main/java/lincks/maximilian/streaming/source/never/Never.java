package lincks.maximilian.streaming.source.never;

import static lincks.maximilian.streaming.stage.StageChain.$;
import static lincks.maximilian.streaming.stage.Stages.buffer;

import lincks.maximilian.streaming.source.Source;
import lincks.maximilian.streaming.stage.Stage;

public sealed interface Never permits NeverImpl {

  static <T> Stage<Never, T> asNeverStage(Source<T> source) {
    // pull the never source to make it return empty on the next class
    return $((never) -> () -> never.pull().map(_ -> source), buffer());
  }

  static <T> Source<T> fromNever(Stage<Never, T> stage) {
    // we need one value to make the never source pullable
    return Source.<Never>of(new NeverImpl()).then(stage);
  }
}
