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

    @SuppressLint("CheckResult")
    public void refreshDevice(final int deviceId) {
        api
                .getDevice(auth.id, deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        deviceEntity -> db.deviceDao().upsert(deviceEntity)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        deviceEntity -> {},
                        throwable -> Log.e(TAG, "Could not refresh device.", throwable)
                );
    }

    public void refreshAllLocalData() {
        Single
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
