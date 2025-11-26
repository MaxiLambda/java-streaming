package lincks.maximilian.streaming.source;

import lincks.maximilian.streaming.Source;

import java.util.Optional;

public interface AppendableSource<T> extends Source<T> {
    AppendableSource<T> append(T element);

    @Override
    default AppendableSource<T> concat(Source<T> other) {
        Source<T> previous = Source.<T>empty().concat(this).concat(other);
        AppendableSource<T> ret = new AppendableSourceImpl<>();
        return new AppendableSource<T>() {

            Source<T> inner = previous.concat(ret);

            @Override
            public AppendableSource<T> append(T element) {
                ret.append(element);
                return this;
            }

            @Override
            public Optional<T> pull() {
                return inner.pull();
            }
        };
    }
}
