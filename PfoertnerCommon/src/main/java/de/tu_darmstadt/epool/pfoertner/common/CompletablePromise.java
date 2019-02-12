package de.tu_darmstadt.epool.pfoertner.common;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompletablePromise<T> implements Promise<T> {
    private final List<CompletablePromise> subPromises = new LinkedList<>();
    private final List<Function<T, ?>> subFuns = new LinkedList<>();
    private final List<Consumer<Exception>> exceptionHandlers = new LinkedList<>();

    private T result = null;
    private Exception exception = null;

    @SuppressWarnings("unchecked")
    public void complete(final @NonNull T result) {
        if (this.result != null) {
            throw new IllegalStateException("Cant complete this promise, since it has already been completet.");
        }

        else if (this.exception != null) {
            throw new IllegalStateException("Cant complete this promise, since it has already been abortet.");
        }

        this.result = result;

        for (int i = 0; i < subPromises.size(); ++i) {
            final Object nextResult = subFuns.get(i).apply(result);
            subPromises.get(i).complete(nextResult);
        }

        subPromises.clear();
        subFuns.clear();
        exceptionHandlers.clear();
    }

    public void abort(final Exception e) {
        if (this.result != null) {
            throw new IllegalStateException("Cant abort this promise, since it has already been completet.");
        }

        else if (this.exception != null) {
            throw new IllegalStateException("Cant abort this promise, since it has already been abortet.");
        }

        this.exception = e;

        for (final Consumer<Exception> exceptionHandler : this.exceptionHandlers) {
            exceptionHandler.accept(e);
        }

        subPromises.clear();
        subFuns.clear();
        exceptionHandlers.clear();
    }

    @Override
    public <R> Promise<R> then(Function<T, R> fun) {
        final CompletablePromise<R> nextPromise = new CompletablePromise<>();

        if (this.result == null && this.exception == null) {
            this.subPromises.add(nextPromise);
            this.subFuns.add(fun);
        }

        else if (this.result != null) {
            final R nextResult = fun.apply(this.result);

            nextPromise.complete(nextResult);
        }

        return nextPromise;
    }

    @Override
    public Promise<T> onException(Consumer<Exception> consumer) {
        if (this.result == null && this.exception == null) {
            this.exceptionHandlers.add(consumer);
        }

        else if (this.exception != null) {
            consumer.accept(this.exception);
        }

        return this;
    }
}
