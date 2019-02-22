package de.tu_darmstadt.epool.pfoertner.common;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Promise<T> {
    public <R> Promise<R> then(final Function<T, R> fun);

    public default void then(final Consumer<T> consumer) {
        then(result -> {
            consumer.accept(result);

            return null;
        });
    }

    public Promise<T> onException(final Consumer<Exception> fun);
}
