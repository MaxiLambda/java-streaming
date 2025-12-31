package lincks.maximilian.functional.tuple;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

//copied from my functional-support project

/**
 * A generic immutable tuple, implemented as a record.
 */
public record Tuple<X, Y>(X fst, Y snd) {

    /**
     * Maps the first value of the tuple.
     */
    public <T> Tuple<T, Y> mapFst(Function<X, T> f) {
        return new Tuple<>(f.apply(fst), snd);
    }

    /**
     * Maps the second value of the tuple.
     */
    public <T> Tuple<X, T> mapSnd(Function<Y, T> f) {
        return new Tuple<>(fst, f.apply(snd));
    }

    /**
     * Maps the first value of the tuple.
     */
    public <T> Tuple<T, Y> mapFst(BiFunction<X, Y, T> f) {
        return new Tuple<>(f.apply(fst, snd), snd);
    }

    /**
     * Maps the second value of the tuple.
     */
    public <T> Tuple<X, T> mapSnd(BiFunction<X, Y, T> f) {
        return new Tuple<>(fst, f.apply(fst, snd));
    }

    /**
     * Map this tuple to a new one. Each value is mapped with the corresponding function.
     */
    public <T1, T2> Tuple<T1, T2> map(Function<X, T1> f1, Function<Y, T2> f2) {
        return new Tuple<>(f1.apply(fst), f2.apply(snd));
    }

    /**
     * Map this tuple to a new one. Each value is mapped with the corresponding function.
     */
    public <T1, T2> Tuple<T1, T2> map(BiFunction<X, Y, T1> f1, BiFunction<X, Y, T2> f2) {
        return new Tuple<>(f1.apply(fst, snd), f2.apply(fst, snd));
    }


    /**
     * Map this tuple to a new one.
     */
    public <T1, T2> Tuple<T1, T2> map(Function<Tuple<X, Y>, Tuple<T1, T2>> f) {
        return f.apply(this);
    }

    /**
     * Reduce this tuple based on a {@link BiFunction}.
     */
    public <R> R map(BiFunction<X, Y, R> f) {
        return f.apply(fst, snd);
    }

    /**
     * Swap the tuples values.
     */
    public Tuple<Y, X> swap() {
        return new Tuple<>(snd, fst);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(fst, tuple.fst) && Objects.equals(snd, tuple.snd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fst, snd);
    }

}
