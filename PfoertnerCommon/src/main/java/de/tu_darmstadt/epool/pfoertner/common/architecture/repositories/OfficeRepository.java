package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides access to {@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office}
 * data and methods to modify that data.
 * <p>
 * It loosely implements the Repository Pattern (@see
 * <a href="http://msdn.microsoft.com/en-us/library/ff649690.aspx">link</a>
 * ) and therefore abstracts from the data sources
 * (database / network calls to the server).
 * <p>
 * Through this class, the database is enforced as single source of truth, all data accessible
 * through this repository, even when obtained from the network, has been written to the database
 * first.
 * This shall minimize data inconsistencies within the application.
 */
public class OfficeRepository {
    private static final String TAG = "OfficeRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    /**
     * Internal helper function, which helps to cast the Office database entity
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity}) to the
     * corresponding model interface it implements
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office}).
     * <p>
     * This way, clients wont have access to database specific functionality, since the
     * Repository Pattern wants to abstract from it.
     *
     * @param entity to be cast to model interface
     * @return same entity, but as model interface type
     */
    private static Office toInterface(final OfficeEntity entity) {
        return entity; // implicit cast to interface
    }

    /**
     * Creates a repository instance.
     *
     * @param api  used for network calls if information is not available locally or needs to
     *             refreshed
     * @param auth authentication data necessary to use network api
     * @param db   database where data is being cached
     */
    public OfficeRepository(final PfoertnerApi api, final Authentication auth,
                            final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    /**
     * Retrieves Office data by the id of the office. Since it returns {@link LiveData}, the
     * retrieved data is always refreshed, as soon as the value in the repository changes.
     * <p>
     * Calling this method will initiate an asynchronous network refresh, but until that is
     * finished, the LiveData will contain the old cached data, or null.
     *
     * @param officeId id of the office for which the data model shall be retrieved.
     * @return auto-refreshing office data
     */
    public LiveData<Office> getOffice(final int officeId) {
        refreshOffice(officeId);

        return Transformations.map(
                db.officeDao().load(officeId),
                OfficeRepository::toInterface
        );
    }

    /**
     * Same as {@link #getOffice}, but it will provide only the currently available office data
     * and wont auto-refresh.
     * <p>
     * If there isn't any office data available, a network call will be performed and it will
     * return the result of that call.
     *
     * @param officeId id of the office for which the data model shall be retrieved.
     * @return office data for the given id
     */
    public Single<Office> getOfficeOnce(final int officeId) {
        refreshOffice(officeId);

        return db
                .officeDao()
                .loadOnce(officeId)
                .subscribeOn(Schedulers.io())
                .onErrorResumeNext(
                        refreshOfficeSingle(officeId)
                )
                .map(OfficeRepository::toInterface)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Sets a new status string for the office with the given data. The change will be propagated
     * to the server and all other devices, which use the office.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param officeId  id of the office, for which the status shall be changed
     * @param newStatus new status to be set for the office
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setStatus(final int officeId, final String newStatus) {
        return modify(
                officeId,
                officeEntity -> officeEntity.setStatus(newStatus)
        );
    }

    /**
     * Sets a new room name string for the office with the given data. The change will be propagated
     * to the server and all other devices, which use the office.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param officeId id of the office, for which the room name shall be changed
     * @param newRoom  new status to be set for the office
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setRoom(final int officeId, final String newRoom) {
        return modify(
                officeId,
                officeEntity -> officeEntity.setRoom(newRoom)
        );
    }

    /**
     * Internal helper method, which is used to update office data on the server. It is usually
     * employed by setter methods.
     * <p>
     * It will use the locally cached office as base for the update.
     *
     * @param officeId id of office of which data shall be changed
     * @param modifier a function, which applies changes to the current state of the office data
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    private Completable modify(final int officeId, final Consumer<OfficeEntity> modifier) {
        return db
                .officeDao()
                .loadOnce(officeId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify office " + officeId + ", since " +
                                "the office could not be found in the database.", throwable)
                )
                .flatMap(
                        office -> {
                            final OfficeEntity replacement = office.deepCopy();

                            modifier.accept(replacement);

                            return api
                                    .patchOffice(
                                            auth.id,
                                            office.getId(),
                                            replacement
                                    );
                        }
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify office " + officeId + ", since " +
                                "the new data could not be uploaded.", throwable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement();
    }

    /**
     * Asynchronously retrieves office data from the server and inserts it into the local database.
     *
     * @param officeId id of the office whose data shall be retrieved
     * @return the data of the requested office
     */
    private Single<OfficeEntity> refreshOfficeSingle(final int officeId) {
        return api
                .getOffice(auth.id, officeId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        officeEntity -> {
                            Log.d(
                                    TAG,
                                    "Successfully retrieved new information about office " +
                                            officeId +
                                            ". This is the new info: " +
                                            "(id: " +
                                            officeEntity.getId() +
                                            ", Status: " +
                                            officeEntity.getStatus() +
                                            ") Will now insert it into the db."
                            );

                            db.officeDao().upsert(officeEntity);
                        }
                )
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Will asynchronously refresh the locally available office data for the office with the
     * given id.
     *
     * @param officeId id of the office, whose data shall be retrieved
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshOffice(final int officeId) {
        Log.d(TAG, "About to refresh office with id " + officeId);

        return refreshOfficeSingle(officeId)
                .subscribe(
                        officeEntity -> {
                        },
                        throwable -> Log.e(TAG, "Could not refresh office.", throwable)
                );
    }

    /**
     * Same as {@link #refreshOffice}, but will refresh the data of all offices, which are
     * currently cached locally.
     *
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshAllLocalData() {
        Log.d(TAG, "About to refresh all data of already known offices...");

        return Single
                .fromCallable(
                        db.officeDao()::getAllOffices
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        offices -> offices.forEach(
                                officeEntity -> refreshOffice(officeEntity.getId())
                        ),
                        throwable -> Log.e(TAG, "Could not refresh all offices.", throwable)
                );
    }
}
