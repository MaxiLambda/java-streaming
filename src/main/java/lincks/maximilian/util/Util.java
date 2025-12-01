package lincks.maximilian.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface Util {
    static <R,T> BiFunction<R,T,R> fluent(BiConsumer<R,T> consumer) {
        return (r,t) -> {consumer.accept(r,t); return r;};
    }
}
