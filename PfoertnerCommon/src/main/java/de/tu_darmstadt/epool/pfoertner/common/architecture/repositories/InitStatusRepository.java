package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.InitStatusEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.InitStatus;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class InitStatusRepository {
    private static final String TAG = "InitStatusRepository";

    private AppDatabase db;

    public InitStatusRepository(final AppDatabase db) {
        this.db = db;
    }

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

    public Completable setJoinedOfficeId(final int officeId) {
        return modify(
                initStatusEntity -> new InitStatusEntity(
                        initStatusEntity.getId(),
                        officeId
                )
        );
    }

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
