package de.tu_darmstadt.epool.pfoertnerpanel.services;

import android.arch.lifecycle.LifecycleService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.helpers.MemberCollectionsDiffTool;
import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MemberCalendarInfoService extends LifecycleService {
    private static final String TAG = "MemberCalendarInfoService";

    private CompositeDisposable disposables;
    private MemberCollectionsDiffTool membersDiffTool;

    private class UpdatedMemberWork {
        final int memberId;
        final String serverAuthCode;

        public UpdatedMemberWork(int memberId, String serverAuthCode) {
            this.memberId = memberId;
            this.serverAuthCode = serverAuthCode;
        }
    }

    private PublishSubject<UpdatedMemberWork> workQueue;

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

        if (workQueue == null) {
            workQueue = PublishSubject.create();
        }

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

    private void reactToMembersChange(final List<Member> members) {
        final PanelApplication app = PanelApplication.get(this);

        membersDiffTool.insert(members);

        for (final Member member : membersDiffTool.getInserted()) {
            Log.d(TAG, "Observing member " + member.getId());

            app
                    .getRepo()
                    .getMemberRepo()
                    .getMember(member.getId())
                    .observe(this, observedMember -> {
                        workQueue.onNext(
                                new UpdatedMemberWork(observedMember.getId(), observedMember.getServerAuthCode())
                        );
                    });
        }
    }

    private Completable processWork(final UpdatedMemberWork work) {
        final PanelApplication app = PanelApplication.get(this);

        return
                app
                        .getPanelRepo()
                        .getMemberCalendarInfoRepo()
                        .getCalendarInfoByMemberIdOnce(work.memberId)
                        .doOnError(
                                throwable -> Log.e(TAG, "There is no calendar info object. Can not check auth code.", throwable)
                        )
                        .flatMapCompletable(
                                calendarInfo -> {
                                    if (    work.serverAuthCode != null
                                            && (
                                            calendarInfo.getServerAuthCode() == null || !work.serverAuthCode.equals(calendarInfo.getServerAuthCode())
                                    )
                                            ) {
                                        Log.d(TAG, "The server calendar auth code changed, so we will request a new oauth token.");

                                        return fetchNewOAuthToken(work.memberId, work.serverAuthCode)
                                                .doOnSuccess(
                                                        newOAuthToken -> Log.d(TAG, "Received new oauth token from google for member " + work.memberId + ": " + newOAuthToken)
                                                )
                                                .flatMapCompletable(
                                                        newOAuthToken ->
                                                                app
                                                                        .getCalendarApi()
                                                                        .getCredential(newOAuthToken)
                                                                        .doOnSuccess(
                                                                                credential -> Log.d(TAG, "Received credentials from google server for member " + work.memberId)
                                                                        )
                                                                        .flatMapCompletable(
                                                                                credential ->
                                                                                        app
                                                                                                .getCalendarApi()
                                                                                                .getCalendarId(credential)
                                                                                                .doOnSuccess(
                                                                                                        calendarId -> Log.d(TAG, "Got calendar id for member calendar of member " + work.memberId + ": " + calendarId)
                                                                                                )
                                                                                                .flatMapCompletable(
                                                                                                        calendarId -> this.saveCalendarInfo(work.memberId, work.serverAuthCode, newOAuthToken, calendarId)
                                                                                                )
                                                                                                .doOnComplete(
                                                                                                        () -> Log.d(TAG, "Successfully saved new calendar id, server auth code and oauth token for member " + work.memberId)
                                                                                                )
                                                                        )
                                                )
                                                .doOnComplete(
                                                        () -> Log.d(TAG, "Successfully ran full calendar info update on new access token for member " + work.memberId)
                                                )
                                                .doOnError(
                                                        throwable -> Log.e(TAG, "Failed to update calendar info on new access token for member " + work.memberId, throwable)
                                                );
                                    }

                                    else {
                                        Log.d(TAG, "No change of auth code, so we will do nothing.");

                                        return Completable.complete();
                                    }
                                }
                        );
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

        // ensure sequential processing of changes to the serverAuthCode
        disposables.add(
                workQueue
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .flatMapCompletable(this::processWork)
                    .subscribe(
                            () -> Log.d(TAG, "Successfully finished work item."),
                            throwable -> Log.e(TAG, "Failed to process work queue.", throwable)
                    )
        );

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
                        .doOnError(
                                throwable -> Log.e(TAG, "Failed to fetch new oauth token for member " + memberId + " and server auth code " + serverAuthCode, throwable)
                        )
                        .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable saveCalendarInfo(final int memberId, final String serverAuthCode, final String oAuthToken, final String calendarId) {
        final PanelApplication app = PanelApplication.get(this);

        // TODO: Now that calendarId is part of Member, we can remove it from calendarInfo
        return Completable.mergeArray(
           // save server auth code and oauth token
           app
                .getPanelRepo()
                .getMemberCalendarInfoRepo()
                .setOAuthToken(memberId, serverAuthCode, oAuthToken),
           //save calendarId
           app
                .getPanelRepo()
                .getMemberCalendarInfoRepo()
                .setCalendarId(memberId, calendarId),
           app
                .getRepo()
                .getMemberRepo()
                .setCalendarId(memberId, calendarId)
        );
    }
}
