package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.InitStatusEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.InitStatus;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides access to {@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.InitStatus}
 * data and methods to modify that data.
 *
 * The init status needs to be stored, so that the App can for example remember, which office it
 * joined during the initialization.
 *
 * It loosely implements the Repository Pattern (@see
 * <a href="http://msdn.microsoft.com/en-us/library/ff649690.aspx">link</a>
 * ) and therefore abstracts from the data source (database).
 */
public class InitStatusRepository {
    private static final String TAG = "InitStatusRepository";

    private AppDatabase db;

    /**
     * Creates a repository instance.
     *
     * @param db database where data is being stored
     */
    public InitStatusRepository(final AppDatabase db) {
        this.db = db;
    }

    /**
     * Returns the stored initialization data
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.InitStatus})
     * or a newly created InitStatus object, if there is none stored.
     *
     * @return the stored initialization status
     */
    public Single<? extends InitStatus> getInitStatus() {
        return db
                .initStatusDao()
                .get()
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(
                        throwable -> {
                            final InitStatusEntity initStatus = new InitStatusEntity();

                            db
                                    .initStatusDao()
                                    .insert(initStatus);

                            return Single.just(initStatus);
                        }
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Could neither load an InitStatus, nor create a new one. Falling back to dummy instance.", throwable)
                )
                .onErrorResumeNext(Single.just(new InitStatusEntity()))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Stores the id of the office the application joined.
     *
     * @param officeId  id of the office, which the application joined
     * @return a Completable, which indicates, when the database has been successfully changed.
     */
    public Completable setJoinedOfficeId(final int officeId) {
        return modify(
                initStatusEntity -> new InitStatusEntity(
                        initStatusEntity.getId(),
                        officeId
                )
        );
    }

    /**
     * Internal helper method, which is used to update the stored init status. It is usually
     * employed by setter methods.
     * <p>
     * It will use current init status as a base for the update.
     *
     * @param modifier a function, which applies changes to the currently stored init status
     * @return a Completable, which indicates, whether init status has been changed in the db yet.
     */
    private Completable modify(final Function<InitStatusEntity, InitStatusEntity> modifier) {
        return db
                .initStatusDao()
                .get()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(
                        initStatus -> {
                            db
                                    .initStatusDao()
                                    .update(modifier.apply(initStatus)); // updates are synchronous

                            return Completable.complete();
                        }

                )
                .doOnError(
                        throwable -> Log.e(TAG, "Failed update init status.", throwable)
                )
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
