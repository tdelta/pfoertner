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

import PfoertnerCommon.R;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.FcmTokenCreationData;
import io.reactivex.disposables.CompositeDisposable;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";
    private EventChannel eventChannel;

    private CompositeDisposable disposables;

    private void registerToken(final String token) {
        Log.d(TAG,"About to upload new token.");

        final PfoertnerApplication app = PfoertnerApplication.get(MessagingService.this);

        disposables.add(
                app
                        .observeInitialization()
                        .retry()
                        .subscribe(
                                () -> {
                                    // TODO Redo with rxjava and enable retry
                                    new RequestTask<Void>() {
                                        @Override
                                        protected Void doRequests() {
                                            // TODO: Race conditions mit Main?
                                            final PfoertnerApplication app = PfoertnerApplication.get(MessagingService.this);

                                            try {
                                                app.getService()
                                                        .setFcmToken(app.getAuthentication().id, app.getDevice().id, new FcmTokenCreationData(token))
                                                        .execute();
                                            }

                                            catch (final IOException e) {
                                                Log.e(TAG, "Could not upload the FCM token", e);

                                                throw new RuntimeException("Could not upload the FCM token");
                                            }

                                            return null;
                                        }

                                        //TODO, was tun bei onException?
                                    }.execute();

                                    Log.d(TAG,"Uploaded new token.");
                                },
                                throwable -> {
                                    Log.e(TAG, "Could not register token, since app initialization failed. Will retry.", throwable);
                                }
                        )
        );
    }

    private void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(MessagingService.this);

        disposables.add(
                app
                        .init()
                        .subscribe(
                                () -> {},
                                throwable -> {
                                    Log.e(TAG, "Failed to initialize app. Server might be unreachable. Will retry.", throwable);

                                    // Maybe slow down retries..?
                                    init();
                                }
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
        super.onDestroy();

        disposables.dispose();
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
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