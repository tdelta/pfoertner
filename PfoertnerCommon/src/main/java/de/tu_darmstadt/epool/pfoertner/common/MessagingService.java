package de.tu_darmstadt.epool.pfoertner.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.FcmTokenCreationData;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";
    private EventChannel eventChannel;

    private CompositeDisposable disposables;

    private void registerToken(final String token) {
        Log.d(TAG,"About to upload new token.");

        final PfoertnerApplication app = PfoertnerApplication.get(MessagingService.this);

        // Attention: Do not use any class attributes here, since this observer might outlive the service.
        app
                .observeInitialization()
                .doOnSubscribe(
                        disposable -> Log.d(TAG, "Waiting, until the app is initialized, so that we may try to upload the token.")
                )
                .andThen(
                    Completable.fromAction(() -> {
                        Log.d(TAG, "The app has been initialized, so we will try to upload the token right now.");

                        app.getService()
                                .setFcmToken(app.getAuthentication().id, app.getDevice().id, new FcmTokenCreationData(token))
                                .execute();
                    })
                        .subscribeOn(Schedulers.io())
                        .doOnError(
                                throwable -> Log.e(TAG, "Could not upload the FCM token", throwable)
                        )
                        .retry()
                        .doOnComplete(() -> Log.d(TAG,"Successfully uploaded new token."))
                )
                .subscribe(
                        () -> Log.d(TAG, "Completed token registration."),
                        throwable -> {
                            Log.e(TAG, "Could not register token, this should not happen. Cant retry (anymore)", throwable);
                        }
                );
    }

    private void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(MessagingService.this);

        disposables.add(
                app
                        .init()
                        .retryWhen(
                                throwables -> throwables.flatMap(
                                        throwable -> {
                                            Log.e(TAG, "Failed to initialize app. Server might be unreachable. Will retry.", throwable);

                                            return Flowable.timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread());
                                        }
                                )
                        )
                        .subscribe(
                                () -> Log.d(TAG, "Successfully initialized app from MessagingService."),
                                throwable -> Log.e(TAG, "Failed to initialize app. Something went horribly wrong, cant retry.", throwable)
                        )
        );
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Messaging service startet.");
        eventChannel = new EventChannel(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("appointments", "Appointment Requests", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for new appointments requested at the door panel");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        if (disposables != null) {
            disposables.dispose();
        }

        disposables = new CompositeDisposable();

        init();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "The messaging service is being destroyed.");
        super.onDestroy();

        Log.d(TAG, "Disposing observers...");
        disposables.dispose();
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        // TODO: What if SyncService is not running, while we receive an important notification?
        //       Throwing it into the EventChannel will cause it to get lost.
        {
            final Gson gson = new Gson();
            Log.d(TAG, "Received FCM message. It contains: " + gson.toJson(remoteMessage.getData()));
        }

        if(remoteMessage.getData().containsKey("notification")){
            Log.d(TAG,"Building a notification");
            NotificationHelper.displayNotification(remoteMessage.getData().get("notification"),this);
        }

        else if (remoteMessage.getData().containsKey("event")) {
            try {
                eventChannel.send(
                        EventChannel.EventType.valueOf(
                                remoteMessage.getData().get("event")
                        ),
                        remoteMessage.getData().getOrDefault("payload", null)
                );
            }

            catch (final IllegalArgumentException e) {
                e.printStackTrace();

                Log.d(TAG, "Server did send unknown event: " + remoteMessage.getData().get("event"));
            }
        }

        else if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Received data notification without event or notification key.");
        }
    }

    @Override
    public void onNewToken(final String token) {
        Log.d(TAG, "Refreshed token: " + token);

        registerToken(token);
    }
}