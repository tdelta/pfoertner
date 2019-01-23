package de.tu_darmstadt.epool.pfoertner.common;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.spencerwi.either.Either;

import java.util.function.Consumer;

public class RequestTask<R> {
    private final Worker<R> worker;
    private Consumer<Void> onDoneCB = null;
    private boolean done = false;

    public RequestTask() {
        this.worker = new Worker<>(
                this
        );
    }

    public final void execute() {
        done = false;
        this.worker.execute();
    }

    public final void whenDone(final @Nullable Consumer<Void> onDoneCB) {
        this.onDoneCB = onDoneCB;

        if (this.done) {
            onDoneCB.accept(null);
        }
    }

    protected R doRequests() throws Exception {
       return null;
    }

    protected void onSuccess(final R result) {

    }

    protected void onException(final Exception e) {

    }

    protected void onDone() {

    }

    private static class Worker<R> extends AsyncTask<Void, Void, Either<Exception, R>> {
        private final RequestTask<R> parent;

        Worker(final RequestTask<R> parent) {
            this.parent = parent;
        }

        @Override
        protected Either<Exception, R> doInBackground(final Void ... parameters) {
            try {
                return Either.right(
                        parent.doRequests()
                );
            }

            catch (final Exception e) {
                e.printStackTrace();

                return Either.left(e);
            }
        }

        @Override
        protected void onPostExecute(final Either<Exception, R> eitherExceptionOrResult) {
            eitherExceptionOrResult.run(
                    parent::onException,
                    parent::onSuccess
            );

            this.parent.done = true;

            if (this.parent.onDoneCB != null) {
                this.parent.onDoneCB.accept(null);
            }

            this.parent.onDone();
        }
    }
}
