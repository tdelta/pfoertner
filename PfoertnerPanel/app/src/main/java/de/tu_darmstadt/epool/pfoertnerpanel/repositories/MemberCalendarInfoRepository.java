package de.tu_darmstadt.epool.pfoertnerpanel.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import de.tu_darmstadt.epool.pfoertnerpanel.db.PanelDatabase;
import de.tu_darmstadt.epool.pfoertnerpanel.db.entities.MemberCalendarInfoEntity;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MemberCalendarInfoRepository {
    private static final String TAG = "MemberCalendarInfoRepository";
    private final PanelDatabase db;

    public MemberCalendarInfoRepository(final PanelDatabase db) {
        this.db = db;
    }

    @SuppressWarnings("CheckResult")
    public LiveData<? extends MemberCalendarInfo> getCalendarInfoByMemberId(final int memberId) {
        createIfNotPresent(memberId)
                .subscribe(
                        () -> {},
                        throwable -> Log.e(TAG, "Could not retrieve calendar info for member " +  memberId + ", since creating it failed.", throwable)
                );

        return db
                .memberCalendarInfoDao()
                .load(memberId);
    }

    public Single<MemberCalendarInfo> getCalendarInfoByMemberIdOnce(final int memberId) {
        return createIfNotPresent(memberId)
                .andThen(
                 db
                    .memberCalendarInfoDao()
                    .loadOnce(memberId)
                    .subscribeOn(Schedulers.io())
                    .map(
                            entity -> entity // convert to interface
                    )
                );
    }

    private Completable createIfNotPresent(final int memberId) {
        return Completable.fromRunnable(
                () -> db.memberCalendarInfoDao().insert(
                        new MemberCalendarInfoEntity(memberId)
                ) // insert ignores errors and does nothing
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(
                    () -> Log.d(TAG, "Successfully retrieved calendar info for member " + memberId)
                )
                .doOnError(
                    throwable -> Log.e(TAG, "Failed to create new calendar info for member " + memberId, throwable)
                );
    }

    public Completable setOAuthToken(final int memberId, final String serverAuthCode, final String oAuthToken) {
        return modifyCalendarInfo(memberId, calendarInfoEntity ->
            new MemberCalendarInfoEntity(
                    calendarInfoEntity.getMemberId(),
                    calendarInfoEntity.getCalendarId(),
                    serverAuthCode,
                    oAuthToken,
                    calendarInfoEntity.getEMail()
            )
        )
                .doOnError(
                        throwable -> Log.e(TAG, "Failed to supplement calendar info of member " + memberId + " with new oAuthToken " + oAuthToken + ".", throwable)
                );
    }

    public Completable setCalendarId(final int memberId, final String newCalendarId) {
        return modifyCalendarInfo(memberId, calendarInfoEntity ->
                new MemberCalendarInfoEntity(
                        calendarInfoEntity.getMemberId(),
                        newCalendarId,
                        calendarInfoEntity.getServerAuthCode(),
                        calendarInfoEntity.getOAuthToken(),
                        calendarInfoEntity.getEMail()
                )
        )
                .doOnError(
                        throwable -> Log.e(TAG, "Failed to supplement calendar info of member " + memberId + " with new calendar id " + newCalendarId + ".", throwable)
                );
    }

    private Completable modifyCalendarInfo(final int memberId, final Function<MemberCalendarInfoEntity, MemberCalendarInfoEntity> modifier) {
        return
                db
                        .memberCalendarInfoDao()
                        .loadOnce(memberId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMapCompletable(
                                calendarInfo -> Completable.fromAction(
                                        () -> db
                                                .memberCalendarInfoDao()
                                                .upsert(
                                                        modifier.apply(calendarInfo)
                                                )
                                )
                        )
                        .doOnComplete(
                                () -> Log.d(TAG, "Successfully modified calendar info of member " + memberId)
                        )
                        .doOnError(
                                throwable -> Log.e(TAG, "Failed to retrieve calendar info of member " + memberId + " to modify it.", throwable)
                        )
                        .observeOn(AndroidSchedulers.mainThread());
    }
}
