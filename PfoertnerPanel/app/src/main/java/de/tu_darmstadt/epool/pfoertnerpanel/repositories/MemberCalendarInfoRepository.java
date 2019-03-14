package de.tu_darmstadt.epool.pfoertnerpanel.repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.UUID;
import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.WebhookRequest;
import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import de.tu_darmstadt.epool.pfoertnerpanel.db.PanelDatabase;
import de.tu_darmstadt.epool.pfoertnerpanel.db.entities.MemberCalendarInfoEntity;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
                    } else if(calendarInfo.oauthTokenHasExpired()) {
                        refreshOAuthToken(calendarInfo).subscribe(
                                () -> Log.d(TAG,"Successfully refreshed oauth code from google servers"),
                                throwable -> Log.e(TAG,"Error refreshing oauth code from google servers",throwable)
                        );
                    } else {
                        if(calendarInfo.webhookHasExpired()){
                            refreshWebhook(calendarInfo).subscribe(
                                    () -> Log.d(TAG,"Successfully refreshed webhook"),
                                    throwable -> Log.e(TAG,"Error refreshing webhook",throwable)
                            );
                        }
                        calendarInfoFilter.setValue(calendarInfo);
                    }
                }
            );
        return calendarInfoFilter;
    }

    private Completable refreshWebhook(MemberCalendarInfo calendarInfo){
        return app.getRepo()
                .getMemberRepo()
                .getMemberOnce(calendarInfo.getMemberId())
                .flatMapCompletable(
                    member ->{
                        Log.d(TAG,"Member webhook id: "+member.getWebhookId());
                        WebhookRequest webhookRequest = new WebhookRequest(member.getWebhookId());
                        return app.getApi()
                                .requestCalendarWebhook("Bearer "+calendarInfo.getOAuthToken(), calendarInfo.getCalendarId(), webhookRequest)
                                .flatMapCompletable(
                                        webhookResponse ->
                                                setWebhookExpiration(
                                                                calendarInfo.getMemberId(),
                                                                webhookResponse.getExpiration()
                                                        )
                                );

                    }
                )
                .subscribeOn(Schedulers.io());
    }

    private Completable refreshOAuthToken(MemberCalendarInfo calendarInfo){
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
        return modifyCalendarInfo(memberId, calendarInfoEntityCopy ->
                {
                    calendarInfoEntityCopy.setServerAuthCode(serverAuthCode);
                    calendarInfoEntityCopy.setOAuthToken(oAuthToken);
                    calendarInfoEntityCopy.setRefreshToken(refreshToken);
                    calendarInfoEntityCopy.setOauth2TtlMinutes(oAuthTtlMinutes);
                    calendarInfoEntityCopy.setCreated(created);
                }
        )
                .doOnComplete(
                        () -> Log.d(TAG,"Successfully updated oAuth data, refresh-token: "+refreshToken+", ttl: "+oAuthTtlMinutes)
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Failed to supplement calendar info of member " + memberId + " with new oAuthToken " + oAuthToken + ".", throwable)
                );
    }

    public Completable setWebhookExpiration(final int memberId, final long newWebhookExpirationUnixtimestamp){
        final LocalDateTime webhookExpirationDate =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(newWebhookExpirationUnixtimestamp), ZoneId.systemDefault());
        return modifyCalendarInfo(memberId, calendarInfoENtityCopy ->
                    calendarInfoENtityCopy.setWebhookExpiration(webhookExpirationDate)
                );
    }

    public Completable setCalendarId(final int memberId, final String newCalendarId) {
        return modifyCalendarInfo(memberId, calendarInfoEntityCopy ->
                calendarInfoEntityCopy.setCalendarId(newCalendarId)
        )
                .doOnComplete(
                        () -> Log.d(TAG,"Successfully saved calendar id "+newCalendarId+" for member "+memberId)
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Failed to supplement calendar info of member " + memberId + " with new calendar id " + newCalendarId + ".", throwable)
                );
    }

    private Completable modifyCalendarInfo(final int memberId, final Consumer<MemberCalendarInfoEntity> modifier) {
        return
                db
                        .memberCalendarInfoDao()
                        .loadOnce(memberId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMapCompletable(
                                calendarInfo -> {

                                    MemberCalendarInfoEntity copy = calendarInfo.deepCopy();
                                    modifier.accept(copy);

                                    return Completable.fromAction(
                                            () -> db
                                                    .memberCalendarInfoDao()
                                                    .upsert(
                                                            copy
                                                    )
                                    );
                                }
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
