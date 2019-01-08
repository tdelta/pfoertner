package de.tu_darmstadt.epool.pfoertner.common;

import android.os.AsyncTask;

import com.spencerwi.either.Either;

public abstract class RequestTask<R> {
    final private Worker<R> worker;

    public RequestTask() {
        this.worker = new Worker<>(
                this
        );
    }

    public final void execute() {
        this.worker.execute();
    }

    abstract protected R doRequests();

    protected void onSuccess(final R result) {

    }

    protected void onException(final Exception e) {

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
        }
    }
}
