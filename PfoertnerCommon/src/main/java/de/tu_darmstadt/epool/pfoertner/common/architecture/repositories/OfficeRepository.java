package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OfficeRepository {
    private static final String TAG = "OfficeRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    public OfficeRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    public LiveData<? extends Office> getOffice(final int officeId) {
        refreshOffice(officeId);

        return db.officeDao().load(officeId);
    }

    public void patchOffice(final OfficeEntity office) {
        api
                .patchOffice(auth.id, office.getId(), office)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(
                        throwable -> Log.e(TAG, "Failed to patch office with id " + office.getId(), throwable)
                )
                .subscribe();
    }

    @SuppressLint("CheckResult")
    public void refreshOffice(final int officeId) {
        api
                .getOffice(auth.id, officeId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        officeEntity -> db.officeDao().save(officeEntity)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        officeEntity -> {},
                        throwable -> Log.e(TAG, "Could not refresh office.", throwable)
                );
    }
}
