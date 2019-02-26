package de.tu_darmstadt.epool.pfoertnerpanel.services;

import android.arch.lifecycle.LifecycleService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MemberCalendarInfoService extends LifecycleService {
    private static final String TAG = "MemberCalendarInfoService";

    private Disposable memberObserverDisposable;

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        // We do not support binding
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d(TAG, "Started service.");

        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Log.d(TAG, "Started lifecycle of service.");

        final PanelApplication app = PanelApplication.get(this);

        if (memberObserverDisposable != null) {
            memberObserverDisposable.dispose();
        }

        memberObserverDisposable = app
                .observeOfficeId()
                .subscribe(
                        officeId -> {
                            app
                                    .getRepo()
                                    .getMemberRepo()
                                    .getMembersByOffice(officeId)
                                    .observe(
                                            this,
                                            members -> {
                                                for (final Member member : members) {
                                                    app
                                                            .getPanelRepo()
                                                            .getMemberCalendarInfoRepo()
                                                            .getCalendarInfoByMemberId(member.getId())
                                                            .observe(this, calendarInfo -> { // TODO: Was passiert bei mehrfachem observe?
                                                                if (calendarInfo != null) {
                                                                    if (    member.getServerAuthCode() != null
                                                                            && (
                                                                            calendarInfo.getServerAuthCode() == null
                                                                                    || !member.getServerAuthCode().equals(calendarInfo.getServerAuthCode())
                                                                    )
                                                                            ) {
                                                                        fetchNewOAuthToken(member.getId(), member.getServerAuthCode())
                                                                                .flatMap(
                                                                                        serverAuthCode ->
                                                                                            app
                                                                                                    .getCalendarApi()
                                                                                                    .getCredential(serverAuthCode)
                                                                                )
                                                                                .flatMap(
                                                                                        credential ->
                                                                                            app
                                                                                                    .getCalendarApi()
                                                                                                    .getCalendarId(credential)
                                                                                )
                                                                                .flatMapCompletable(
                                                                                        calendarId -> this.setCalendarId(member.getId(), calendarId)
                                                                                )
                                                                                .andThen(notifyServerAboutCalendarInit(member.getId()))
                                                                                .subscribe(
                                                                                        () -> Log.d(TAG, "Successfully ran full calendar info update on new access token for member " + member.getId()),
                                                                                        throwable -> Log.e(TAG, "Failed to update calendar info on new access token for member " + member.getId(), throwable)
                                                                                );
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                    );
                        },
                        throwable -> Log.e(TAG, "Could not register member observers for new office.", throwable)
                );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        memberObserverDisposable.dispose();

        Log.d(TAG, "Destroyed service.");
    }

    @SuppressWarnings("CheckResult")
    private Single<String> fetchNewOAuthToken(final int memberId, final String serverAuthCode) {
        final PanelApplication app = PanelApplication.get(this);

        return app
                        .getCalendarApi()
                        .getAccessToken(serverAuthCode)
                        .subscribeOn(Schedulers.io())
                        .doOnSuccess( oAuthToken -> app
                                .getPanelRepo()
                                .getMemberCalendarInfoRepo()
                                .setOAuthToken(memberId, serverAuthCode, oAuthToken)
                        )
                        .doOnError(
                                throwable -> Log.e(TAG, "Failed to fetch new oauth token for member " + memberId + " and server auth code " + serverAuthCode, throwable)
                        )
                        .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable setCalendarId(final int memberId, final String calendarId) {
        final PanelApplication app = PanelApplication.get(this);

        return app
                    .getPanelRepo()
                    .getMemberCalendarInfoRepo()
                    .setCalendarId(memberId, calendarId);
    }

    private Completable notifyServerAboutCalendarInit(final int memberId) {
        final PanelApplication app = PanelApplication.get(this);

        return app
                .getApi()
                .createdCalendar(
                        app.getAuthentication().id,
                        memberId
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not notify server about calendar initialization for member " + memberId, throwable)
                )
                .doOnComplete(
                        () -> Log.d(TAG, "Successfully notified server about calendar initialization for member " + memberId)
                );
    }
}
