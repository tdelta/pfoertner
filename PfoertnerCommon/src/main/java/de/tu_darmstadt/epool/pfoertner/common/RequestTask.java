package de.tu_darmstadt.epool.pfoertner.common;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.spencerwi.either.Either;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;

public abstract class RequestTask<R> {
    final private Worker<R> worker;

    public RequestTask(
        final SharedPreferences registrationInfo,
        final PfoertnerService service
    ) {
        this.worker = new Worker<>(
                registrationInfo,
                service,
                this
        );
    }

    public final void execute() {
        this.worker.execute();
    }

    abstract protected R doRequests(final SharedPreferences registrationInfo, final PfoertnerService service);

    protected void onSuccess(final R result) {

    }

    protected void onException(final Exception e) {

    }

    private static class Worker<R> extends AsyncTask<Void, Void, Either<Exception, R>> {
        private final SharedPreferences registrationInfo;
        private final PfoertnerService service;
        private final RequestTask<R> parent;

        Worker(final SharedPreferences registrationInfo, final PfoertnerService service, final RequestTask<R> parent) {
            this.registrationInfo = registrationInfo;
            this.service = service;
            this.parent = parent;
        }

        @Override
        protected Either<Exception, R> doInBackground(final Void ... parameters) {
            try {
                return Either.right(
                        parent.doRequests(registrationInfo, service)
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
