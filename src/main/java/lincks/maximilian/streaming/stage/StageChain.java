package lincks.maximilian.streaming.stage;

public interface StageChain {
  // can be used to cast lambdas
  static <T, R> Stage<T, R> $(Stage<T, R> first) {
    return first;
  }

  static <T, A, R> Stage<T, R> $(Stage<T, A> first, Stage<A, R> second) {
    return first.then(second);
  }

  static <T, T2, T3, R> Stage<T, R> $(
      Stage<T, T2> first, Stage<T2, T3> second, Stage<T3, R> third) {
    return first.then(second).then(third);
  }

  static <T, T2, T3, T4, R> Stage<T, R> $(
      Stage<T, T2> first, Stage<T2, T3> second, Stage<T3, T4> third, Stage<T4, R> fourth) {
    return first.then(second).then(third).then(fourth);
  }

  static <T, T2, T3, T4, T5, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, R> fifth) {
    return first.then(second).then(third).then(fourth).then(fifth);
  }

  static <T, T2, T3, T4, T5, T6, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, T6> fifth,
      Stage<T6, R> sixth) {
    return first.then(second).then(third).then(fourth).then(fifth).then(sixth);
  }

  static <T, T2, T3, T4, T5, T6, T7, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, T6> fifth,
      Stage<T6, T7> sixth,
      Stage<T7, R> seventh) {
    return first.then(second).then(third).then(fourth).then(fifth).then(sixth).then(seventh);
  }

  static <T, T2, T3, T4, T5, T6, T7, T8, R> Stage<T, R> $(
      Stage<T, T2> first,
      Stage<T2, T3> second,
      Stage<T3, T4> third,
      Stage<T4, T5> fourth,
      Stage<T5, T6> fifth,
      Stage<T6, T7> sixth,
      Stage<T7, T8> seventh,
      Stage<T8, R> eighth) {
    return first
        .then(second)
        .then(third)
        .then(fourth)
        .then(fifth)
        .then(sixth)
        .then(seventh)
        .then(eighth);
  }
}
