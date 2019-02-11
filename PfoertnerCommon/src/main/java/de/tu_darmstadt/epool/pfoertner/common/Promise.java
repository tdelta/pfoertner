package de.tu_darmstadt.epool.pfoertner.common;

import android.os.AsyncTask;
import android.util.Log;

import com.spencerwi.either.Either;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Promise<T> {
    private static final String TAG = "Promise";

    private Supplier<T> source;
    private List<Consumer<T>> resultHandlers = new LinkedList<>();
    private T result = null;

    public Promise(final Supplier<T> source) {
        this.source = source;

        new Worker<T>(this)
                .execute();
    }

    private void onSuccess(final T result) {
        this.result = result;

        this.resultHandlers.forEach(tConsumer -> tConsumer.accept(result));
        this.resultHandlers.clear();

        // those may now be garbage collected
        this.resultHandlers = null;
        this.source = null;
    }

    private void onException(final Exception e) {
        e.printStackTrace();

        Log.e(TAG, "Failed to fulfill promise because of an exception.", e);
    }

    public void then(final Consumer<T> consumer) {
        if (this.result == null) {
            this.resultHandlers.add(consumer);
        }

        else {
            consumer.accept(this.result);
        }
    }

    private static class Worker<R> extends AsyncTask<Void, Void, Either<Exception, R>> {
        private final Promise<R> parent;

        Worker(final Promise<R> parent) {
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
