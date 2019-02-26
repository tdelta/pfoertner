package de.tu_darmstadt.epool.pfoertnerpanel.services;

import android.arch.lifecycle.LifecycleService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.helpers.MemberCollectionsDiffTool;
import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MemberCalendarInfoService extends LifecycleService {
    private static final String TAG = "MemberCalendarInfoService";

    private CompositeDisposable disposables;
    private MemberCollectionsDiffTool membersDiffTool;

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

        if (disposables != null) {
            disposables.dispose();
        }

        disposables = new CompositeDisposable();
        membersDiffTool = new MemberCollectionsDiffTool();

        waitForAppInit();
    }

    private void waitForAppInit() {
        final PanelApplication app = PanelApplication.get(this);

        disposables.add(app
                .observeInitialization()
                .subscribe(
                        this::observeOfficeInit,
                        throwable -> Log.e(TAG, "App failed to initialize, so we also fail to initialize the observers necessary to run this service properly.", throwable)
                )
        );
    }

    private void reactToMembersChange(final List<MemberEntity> rawMembers) {
        final PanelApplication app = PanelApplication.get(this);

        // TODO tidy that up
        final List<Member> members = new ArrayList<>(rawMembers);
        membersDiffTool.insert(members);

        for (final Member member : membersDiffTool.getInserted()) {
            Log.d(TAG, "Observing member " + member.getId());

            app
                    .getRepo()
                    .getMemberRepo()
                    .getMember(member.getId())
                    .observe(this, observedMember -> {
                        app
                                .getPanelRepo()
                                .getMemberCalendarInfoRepo()
                                .getCalendarInfoByMemberIdOnce(observedMember.getId())
                                .subscribe(
                                        calendarInfo -> {
                                            if (    observedMember.getServerAuthCode() != null
                                                    && (
                                                    calendarInfo.getServerAuthCode() == null || !observedMember.getServerAuthCode().equals(calendarInfo.getServerAuthCode())
                                            )
                                                    ) {
                                                Log.d(TAG, "The server calendar auth code changed, so we will request a new oauth token.");

                                                fetchNewOAuthToken(observedMember.getId(), observedMember.getServerAuthCode())
                                                        .doOnSuccess(
                                                                newOAuthToken -> Log.d(TAG, "Received new oauth token from google for member " + member.getId() + ": " + newOAuthToken)
                                                        )
                                                        .flatMap(
                                                                newOAuthToken ->
                                                                        app
                                                                                .getCalendarApi()
                                                                                .getCredential(newOAuthToken)
                                                        )
                                                        .doOnSuccess(
                                                                credential -> Log.d(TAG, "Received credentials from google server for member " + member.getId())
                                                        )
                                                        .flatMap(
                                                                credential ->
                                                                        app
                                                                                .getCalendarApi()
                                                                                .getCalendarId(credential)
                                                        )
                                                        .doOnSuccess(
                                                                calendarId -> Log.d(TAG, "Got calendar id for member calendar of member " + member.getId() + ": " + calendarId)
                                                        )
                                                        .flatMapCompletable(
                                                                calendarId -> this.setCalendarId(observedMember.getId(), calendarId)
                                                        )
                                                        .doOnComplete(
                                                                () -> Log.d(TAG, "Successfully saved new calendar id for member " + member.getId())
                                                        )
                                                        .andThen(notifyServerAboutCalendarInit(observedMember.getId()))
                                                        .subscribe(
                                                                () -> Log.d(TAG, "Successfully ran full calendar info update on new access token for member " + member.getId()),
                                                                throwable -> Log.e(TAG, "Failed to update calendar info on new access token for member " + member.getId(), throwable)
                                                        );
                                            }
                                        },
                                        throwable -> Log.e(TAG, "There is no calendar info object. Can not check auth code.", throwable)
                                );
                    });
        }
    }

    private void observeMembersChange(final int officeId) {
        final PanelApplication app = PanelApplication.get(this);

        Log.d(TAG, "Observing members of office with id " + officeId);

        app
                .getRepo()
                .getMemberRepo()
                .getMembersFromOffice(officeId)
                .observe(
                        this,
                        this::reactToMembersChange
                );
    }

    private void observeOfficeInit() {
        final PanelApplication app = PanelApplication.get(this);

        disposables.add(app
                .observeOfficeId()
                .subscribe(
                        this::observeMembersChange,
                        throwable -> Log.e(TAG, "Could not register member observers for new office.", throwable)
                )
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        disposables.dispose();

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
