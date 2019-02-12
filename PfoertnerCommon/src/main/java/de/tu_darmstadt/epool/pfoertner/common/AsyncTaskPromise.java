package de.tu_darmstadt.epool.pfoertner.common;

import android.os.AsyncTask;
import android.util.Log;

import com.spencerwi.either.Either;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AsyncTaskPromise<T> implements Promise<T> {
    private static final String TAG = "AsyncTaskPromise";

    private List<CompletablePromise<T>> subPromises = new LinkedList<>();

    private Supplier<T> source;
    private T result = null;
    private Exception exception = null;

    @Override
    public <R> Promise<R> then(Function<T, R> fun) {
        if (this.result == null && this.exception != null) {
            final CompletablePromise<T> subPromise = new CompletablePromise<>();

            this.subPromises.add(subPromise);

            return subPromise.then(fun);
        }

        else if (this.result != null) {
            final CompletablePromise<R> subPromise = new CompletablePromise<>();
            final R nextResult = fun.apply(this.result);

            subPromise.complete(nextResult);

            return subPromise;
        }

        else {
            return new CompletablePromise<>(); // promise can never be resolved, since there has already been an exception
        }
    }

    @Override
    public Promise<T> onException(final Consumer<Exception> fun) {
        if (this.exception == null && this.result != null) {
            final CompletablePromise<T> subPromise = new CompletablePromise<>();

            this.subPromises.add(subPromise);

            return subPromise.onException(fun);
        }

        else if (this.exception != null) {
            fun.accept(this.exception);

            return this;
        }

        else {
            return this; // promise can never fail, since there has already been completed
        }
    }

    public AsyncTaskPromise(final Supplier<T> source) {
        this.source = source;

        new Worker<T>(this)
                .execute();
    }

    private void onSuccess(final T result) {
        this.result = result;

        this.subPromises.forEach(completablePromise -> completablePromise.complete(result));
        this.subPromises.clear();

        // those may now be garbage collected
        this.subPromises = null;
        this.source = null;
    }

    private void onException(final Exception e) {
        e.printStackTrace();

        Log.e(TAG, "Failed to fulfill promise because of an exception.", e);

        this.exception = e;

        this.subPromises.forEach(completablePromise -> completablePromise.abort(e));
        this.subPromises.clear();

        // those may now be garbage collected
        this.subPromises = null;
        this.source = null;
    }

    private static class Worker<R> extends AsyncTask<Void, Void, Either<Exception, R>> {
        private final AsyncTaskPromise<R> parent;

        Worker(final AsyncTaskPromise<R> parent) {
            this.parent = parent;
        }

        @Override
        protected Either<Exception, R> doInBackground(final Void ... parameters) {
            try {
                return Either.right(
                        parent.source.get()
                );
            }

            catch (final Exception e) {
                return Either.left(e);
            }
        }

        @Override
        protected void onPostExecute(final Either<Exception, R> eitherExceptionOrResult) {
            eitherExceptionOrResult.run(
                    parent::onException,
                    parent::onSuccess
            );
        }
    }
}
