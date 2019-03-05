package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class OfficeRepository {
    private static final String TAG = "OfficeRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    private static Office toInterface(final OfficeEntity entity) {
        return entity; // implicit cast to interface
    }

    public OfficeRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    public LiveData<Office> getOffice(final int officeId) {
        refreshOffice(officeId);

        return Transformations.map(
                db.officeDao().load(officeId),
                OfficeRepository::toInterface
        );
    }

    public Completable setStatus(final int officeId, final String newStatus) {
        return modify(
                officeId,
                officeEntity -> officeEntity.setStatus(newStatus)
        );
    }

    public Completable setRoom(final int officeId, final String newRoom) {
        return modify(
                officeId,
                officeEntity -> officeEntity.setRoom(newRoom)
        );
    }

    private Completable modify(final int officeId, final Consumer<OfficeEntity> modifier) {
        return db
                .officeDao()
                .loadOnce(officeId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify office " + officeId + ", since the office could not be found in the database.", throwable)
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
                        throwable -> Log.e(TAG, "Could not modify office " + officeId +  ", since the new data could not be uploaded.", throwable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement();
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
                        officeEntity -> db.officeDao().upsert(officeEntity)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        officeEntity -> {},
                        throwable -> Log.e(TAG, "Could not refresh office.", throwable)
                );
    }
}
