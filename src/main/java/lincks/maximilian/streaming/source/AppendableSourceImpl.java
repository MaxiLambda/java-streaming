package lincks.maximilian.streaming.source;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

public class AppendableSourceImpl<T> implements AppendableSource<T> {
    private final Queue<T> list = new LinkedList<>();

    @Override
    public AppendableSourceImpl<T> append(T element) {
        list.add(element);
        return this;
    }

    @Override
    public Stream<T> toStream() {
        return list.stream();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Optional<T> pull() {
        return Optional.ofNullable(list.poll());
    }
}
