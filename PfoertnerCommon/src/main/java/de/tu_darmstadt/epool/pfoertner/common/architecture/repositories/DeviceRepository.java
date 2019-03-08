package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
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

public class DeviceRepository {
    private static final String TAG = "DeviceRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    private static Device toInterface(final DeviceEntity entity) {
        return entity; //  implicit cast to interface
    }

    public DeviceRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    public LiveData<Device> getDevice(final int deviceId) {
        refreshDevice(deviceId);

        return Transformations.map(
                db.deviceDao().load(deviceId),
                DeviceRepository::toInterface
        );
    }

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
