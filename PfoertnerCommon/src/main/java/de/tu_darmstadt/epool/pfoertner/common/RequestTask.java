package de.tu_darmstadt.epool.pfoertner.common;

import android.os.AsyncTask;
import androidx.annotation.Nullable;

import com.spencerwi.either.Either;

import java.util.function.Consumer;

/**
 * Wrapper class for asynchronous execution like calls to the server.
 * @param <R> Type of the data returned by the asynchronous execution
 */
public class RequestTask<R> {
    private Worker<R> worker;
    private Consumer<Void> onDoneCB = null;
    private boolean done = true;

    public RequestTask() {
    }

    /**
     * Executes the operation specified in doRequests in an io thread.
     */
    public final void execute() {
        if (!done) {
            throw new IllegalStateException("A RequestTask can not be executed twice at the same time. You can only call execute() if the task is not running at the same time.");
        }

        this.worker = new Worker<>(
                this
        );

        done = false;
        this.worker.execute();
    }

    /**
     * Accepts a consumer that is called once, when the task in doRequests is done.
     * @param onDoneCB Consumer
     */
    public final void whenDone(final @Nullable Consumer<Void> onDoneCB) {
        this.onDoneCB = onDoneCB;

        if (this.done) {
            this.onDoneCB = null; // delete old callback, since it should only be called once, when the Task is done
            onDoneCB.accept(null);
        }
    }

    /**
     * Asynchronous task for the RequestTask to execute. Should be implemented when overriding this class
     *
     * @return The result of the asynchronous task
     * @throws Exception Can be handled in onException
     */
    protected R doRequests() throws Exception {
       return null;
    }

    /**
     * Callback for after doRequests is done. Should be implemented when overriding this class
     *
     * @param result The result of doRequests
     */
    protected void onSuccess(final R result) {

    }

    /**
     * Called when an exception occured in doRequests. Should be implemented when overriding this class
     *
     * @param e Exception
     */
    protected void onException(final Exception e) {

    }

    /**
     * Called after the Consumer set in whenDone is finished executing and the RequestTask is completely done.
     */
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

            final Consumer<Void> onDoneCB = this.parent.onDoneCB;
            if (onDoneCB != null) {
                this.parent.onDoneCB = null; // delete old callback, since it should only be called the first time the task finishes
                onDoneCB.accept(null);
            }

            this.parent.onDone();
        }
    }
}
