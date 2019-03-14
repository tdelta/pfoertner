package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.DeviceEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Device;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides access to {@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Device}
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
public class DeviceRepository {
    private static final String TAG = "DeviceRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    /**
     * Internal helper function, which helps to cast the Device database entity
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.DeviceEntity}) to the
     * corresponding model interface it implements
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Device}).
     * <p>
     * This way, clients wont have access to database specific functionality, since the
     * Repository Pattern wants to abstract from it.
     *
     * @param entity to be cast to model interface
     * @return same entity, but as model interface type
     */
    private static Device toInterface(final DeviceEntity entity) {
        return entity; //  implicit cast to interface
    }

    /**
     * Creates a repository instance.
     *
     * @param api  used for network calls if information is not available locally or needs to
     *             refreshed
     * @param auth authentication data necessary to use network api
     * @param db   database where data is being cached
     */
    public DeviceRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    /**
     * Retrieves Device data by the id of the device. Since it returns {@link LiveData}, the
     * retrieved data is always refreshed, as soon as the value in the repository changes.
     * <p>
     * Calling this method will initiate an asynchronous network refresh, but until that is
     * finished, the LiveData will contain the old cached data, or null.
     *
     * @param deviceId id of the device for which the data model shall be retrieved.
     * @return auto-refreshing device data
     */
    public LiveData<Device> getDevice(final int deviceId) {
        refreshDevice(deviceId);

        return Transformations.map(
                db.deviceDao().load(deviceId),
                DeviceRepository::toInterface
        );
    }

    /**
     * Will asynchronously refresh the locally available device data for the device with the
     * given id.
     *
     * @param deviceId id of the device, whose data shall be retrieved
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshDevice(final int deviceId) {
        Log.d(TAG, "About to refresh device...");

        return api
                .getDevice(auth.id, deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        deviceEntity -> {
                            Log.d(
                                    TAG,
                                    "Successfully retrieved new information about device " +
                                            deviceId +
                                            ". This is the new info: " +
                                            "(id: " +
                                            deviceEntity.getId() +
                                            ", fcm token: " +
                                            deviceEntity.getFcmToken() +
                                            ") Will now insert it into the db."
                            );

                            db.deviceDao().upsert(deviceEntity);
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        deviceEntity -> {},
                        throwable -> Log.e(TAG, "Could not refresh device.", throwable)
                );
    }

    /**
     * Same as {@link #refreshDevice}, but will refresh the data of all devices, which are
     * currently cached locally.
     *
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshAllLocalData() {
        Log.d(TAG, "About to refresh all data of already known devices...");

        return Single
                .fromCallable(
                        db.deviceDao()::getAllDevices
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        devices -> devices.forEach(
                                deviceEntity -> refreshDevice(deviceEntity.getId())
                        ),
                        throwable -> Log.e(TAG, "Could not refresh all devices.", throwable)
                );
    }
}
