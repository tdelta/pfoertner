package de.tu_darmstadt.epool.pfoertnerpanel.services;

import androidx.lifecycle.LifecycleService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.List;
import java.util.UUID;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.helpers.MemberCollectionsDiffTool;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.WebhookRequest;
import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Service that listens for changes in office members and retrieves a refresh token,
 * if the office member is connecting his/ her google account.
 */
public class MemberCalendarInfoService extends LifecycleService {
    private static final String TAG = "MemberCalendarInfoService";

    private CompositeDisposable disposables;
    private MemberCollectionsDiffTool membersDiffTool;

    /**
     * Update to the office member to process
     */
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

    /**
     * Android callback, called when the service is started
     * @param intent Intent that started the service
     * @param flags Flags of the intent
     * @param startId Id of the intent
     * @return START_STICKY: Restart the service if it is terminated
     */
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

    /**
     * Calls observerOfficeInit, when the initialization of the app is complete
     */
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

    /**
     * Called when there was an update to an office member. Triggers process work for every member that changed.
     * @param members New office member data
     */
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
                        Log.d(TAG,"Observed Change in office member");
                        workQueue.onNext(
                                new UpdatedMemberWork(observedMember.getId(), observedMember.getServerAuthCode())
                        );
                    });
        }
    }

    /**
     * Called when there was an update to an office member.
     * Checks if the server authentication code changed.
     * If so, requests a new refresh and authentication token from the server.
     * Afterwards, uses the authentication token to load the url of the calendar "Office hours"
     * and requests push notifications for that url via a webhook.
     * Finally saves the gathered information in the database.
     * @param work
     * @return
     */
    private Completable processWork(final UpdatedMemberWork work) {
        final PanelApplication app = PanelApplication.get(this);

        Log.d(TAG, "Starting to process new member update for member " + work.memberId + " and server auth code " + work.serverAuthCode);

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
                                        Log.d(TAG, "Old server auth code: "+calendarInfo.getServerAuthCode()+", new server auth code: "+work.serverAuthCode+"; will request a new oauth token.");

                                        return fetchNewOAuthToken(work.memberId, work.serverAuthCode)
                                                .doOnSuccess(
                                                        newOAuthToken -> Log.d(TAG, "Received new oauth token from google for member " + work.memberId + ": " + newOAuthToken)
                                                )
                                                .flatMapCompletable(
                                                        tokenResponse ->
                                                                app
                                                                        .getCalendarApi()
                                                                        .getCredential(tokenResponse.getAccessToken())
                                                                        .doOnSuccess(
                                                                                credential -> Log.d(TAG, "Received credentials from google server for member " + work.memberId)
                                                                        )
                                                                        .flatMapCompletable(
                                                                            credential ->
                                                                                app
                                                                                    .getCalendarApi()
                                                                                    .getCalendarId(credential)
                                                                                    .flatMapCompletable(
                                                                                            calendarId -> {
                                                                                                Log.d(TAG, "Got calendar id for member calendar of member " + work.memberId + ": " + calendarId);
                                                                                                String webhookId = UUID.randomUUID().toString();
                                                                                                WebhookRequest webhookRequest = new WebhookRequest(webhookId);
                                                                                                return app
                                                                                                        .getApi()
                                                                                                        .requestCalendarWebhook("Bearer "+credential.getAccessToken(), calendarId, webhookRequest)
                                                                                                        .flatMapCompletable(
                                                                                                                (webhookResponse) -> saveCalendarInfo(
                                                                                                                        work.memberId,
                                                                                                                        work.serverAuthCode,
                                                                                                                        tokenResponse,
                                                                                                                        calendarId,
                                                                                                                        webhookResponse.getExpiration(),
                                                                                                                        webhookId
                                                                                                                        )
                                                                                                        );
                                                                                            }
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
                        )
                        .subscribeOn(Schedulers.single());
    }

    /**
     * Called when an office id is retrieved, after the app joined an office
     * @param officeId Id of the office that the observed members belong to.
     */
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

    /**
     * Called when the initialization of the app is done. Starts the work queue that listens to changes in office members.
     */
    private void observeOfficeInit() {
        final PanelApplication app = PanelApplication.get(this);

        // ensure sequential processing of changes to the serverAuthCode
        disposables.add(
                workQueue
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .concatMapCompletable(this::processWork)
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

    /**
     * Android lifecycle method called when the service is destroyed.
     * Disposes all rxjava callbacks.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        disposables.dispose();

        Log.d(TAG, "Destroyed service.");
    }

    /**
     * Makes an asynchronous call to google to retrieve refresh token and an authorization token from a server auth code.
     * @param memberId The id of the office member, that the auth code belongs to
     * @param serverAuthCode The server auth code used to make the request
     * @return A Single that delivers a GoogleTokenResponse, containing refresh and authorization token, when the request to the server is done
     */
    @SuppressWarnings("CheckResult")
    private Single<GoogleTokenResponse> fetchNewOAuthToken(final int memberId, final String serverAuthCode) {
        final PanelApplication app = PanelApplication.get(this);

        return app
                        .getCalendarApi()
                        .getRefreshToken(serverAuthCode)
                        .subscribeOn(Schedulers.io())
                        .doOnError(
                                throwable -> Log.e(TAG, "Failed to fetch new oauth token for member " + memberId + " and server auth code " + serverAuthCode, throwable)
                        )
                        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Asynchronously saves authorization and webhook information in the database.
     * @param memberId Id of the office member that the information belongs to
     * @param serverAuthCode Server authentication code
     * @param tokenResponse Google token response containing refresh token and authorization token
     * @param calendarId Url for the google calendar "Office hours"
     * @param webhookExpiration Unix timestamp of the expiration time of the webhook
     * @param webhookId Id of the webhook for push notifications
     * @return A Completable that calls onComplete, when the database access is done.
     */
    private Completable saveCalendarInfo(final int memberId,
                                         final String serverAuthCode,
                                         final GoogleTokenResponse tokenResponse,
                                         final String calendarId,
                                         final long webhookExpiration,
                                         final String webhookId) {

        final PanelApplication app = PanelApplication.get(this);

        // TODO: Now that calendarId is part of Member, we can remove it from calendarInfo
        return Completable.concatArray(
            // save server auth code and oauth token
            app
                 .getPanelRepo()
                 .getMemberCalendarInfoRepo()
                 .setOAuthToken(memberId,
                         serverAuthCode,
                         tokenResponse.getAccessToken(),
                         tokenResponse.getRefreshToken(),
                         tokenResponse.getExpiresInSeconds()/60,
                         LocalDateTime.now()
                 ),
            app
                 .getPanelRepo()
                 .getMemberCalendarInfoRepo()
                 .setWebhookExpiration(memberId,webhookExpiration),
            //save calendarId
            app
                .getPanelRepo()
                .getMemberCalendarInfoRepo()
                .setCalendarId(memberId, calendarId),
            app
                .getRepo()
                .getMemberRepo()
                .setWebhookId(memberId,webhookId),
            app
                .getRepo()
                .getMemberRepo()
                .setCalendarId(memberId, calendarId)
        )
                .doOnError(
                        throwable -> Log.e(TAG, "Failed to save new calendar information.", throwable)
                )
                .doOnComplete(
                        () -> Log.d(TAG, "Successfully saved new calendar id, server auth code and oauth token for member " + memberId)
                );
    }
}
