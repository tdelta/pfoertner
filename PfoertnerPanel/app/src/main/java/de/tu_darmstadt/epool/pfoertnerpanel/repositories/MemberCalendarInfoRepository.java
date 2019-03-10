package de.tu_darmstadt.epool.pfoertnerpanel.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import de.tu_darmstadt.epool.pfoertnerpanel.webapi.CalendarApi;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import de.tu_darmstadt.epool.pfoertnerpanel.db.PanelDatabase;
import de.tu_darmstadt.epool.pfoertnerpanel.db.entities.MemberCalendarInfoEntity;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MemberCalendarInfoRepository {
    private static final String TAG = "MemberCalendarInfoRepository";
    private final PanelDatabase db;
    private final PanelApplication app;

    public MemberCalendarInfoRepository(final PanelDatabase db, final PanelApplication app) {
        this.db = db;
        this.app = app;
    }

    // TODO: Properly dispose disposables
    @SuppressWarnings("CheckResult")
    public LiveData<MemberCalendarInfo> getCalendarInfoByMemberId(final int memberId) {
        createIfNotPresent(memberId)
                .subscribe(
                        () -> {},
                        throwable -> Log.e(TAG, "Could not retrieve calendar info for member " +  memberId + " from memory, since creating it failed.", throwable)
                );

        LiveData<MemberCalendarInfo> calendarInfoLiveData = Transformations.map(
                db.memberCalendarInfoDao().load(memberId),
                MemberCalendarInfoEntity::toInterface
        );

        MediatorLiveData<MemberCalendarInfo> calendarInfoFilter = new MediatorLiveData<>();
        calendarInfoFilter.addSource(calendarInfoLiveData,
                calendarInfo -> {
                    if(calendarInfo == null){
                        calendarInfoFilter.setValue(null);
                    } else if(calendarInfo.hasExpired()) {
                        refreshOAuthToken(calendarInfo).subscribe(
                                () -> Log.d(TAG,"Successfully refreshed oauth code from google servers"),
                                throwable -> Log.e(TAG,"Error refreshing oauth code from google servers",throwable)
                        );
                    } else {
                        calendarInfoFilter.setValue(calendarInfo);
                    }
                }
            );
        return calendarInfoFilter;
    }

    public Completable refreshOAuthToken(MemberCalendarInfo calendarInfo){
        return app.getCalendarApi()
                .getAccessTokenFromRefreshToken(calendarInfo.getRefreshToken())
                .flatMapCompletable(
                        tokenResponse -> setOAuthToken(calendarInfo.getMemberId(),
                                calendarInfo.getServerAuthCode(),
                                tokenResponse.getAccessToken(),
                                calendarInfo.getRefreshToken(),
                                tokenResponse.getExpiresInSeconds()/60,
                                LocalDateTime.now())
                );
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
                    () -> Log.d(TAG, "Successfully wrote calendar info for member " + memberId + " into persistent memory")
                )
                .doOnError(
                    throwable -> Log.e(TAG, "Failed to create new calendar info for member " + memberId, throwable)
                );
    }

    public Completable setOAuthToken(final int memberId,
                                     final String serverAuthCode,
                                     final String oAuthToken,
                                     final String refreshToken,
                                     final long oAuthTtlMinutes,
                                     LocalDateTime created) {
        return modifyCalendarInfo(memberId, calendarInfoEntity ->
            new MemberCalendarInfoEntity(
                    calendarInfoEntity.getMemberId(),
                    calendarInfoEntity.getCalendarId(),
                    serverAuthCode,
                    oAuthToken,
                    calendarInfoEntity.getEMail(),
                    created,
                    oAuthTtlMinutes,
                    refreshToken
            )
        )
                .doOnComplete(
                        () -> Log.d(TAG,"Successfully updated oAuth data, refresh-token: "+refreshToken+", ttl: "+oAuthTtlMinutes)
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
                        calendarInfoEntity.getEMail(),
                        calendarInfoEntity.getCreated(),
                        calendarInfoEntity.getOauth2TtlMinutes(),
                        calendarInfoEntity.getRefreshToken()
                )
        )
                .doOnComplete(
                        () -> Log.d(TAG,"Successfully saved calendar id "+newCalendarId+" for member "+memberId)
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
