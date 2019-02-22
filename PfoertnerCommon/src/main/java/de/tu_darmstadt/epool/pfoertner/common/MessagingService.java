package de.tu_darmstadt.epool.pfoertner.common;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.FcmTokenCreationData;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";
    private EventChannel eventChannel;

    private RequestTask<Void> initTask =
            new RequestTask<Void>() {
                @Override
                protected Void doRequests() {
                    // TODO: Race conditions mit Main?
                    final PfoertnerApplication app = PfoertnerApplication.get(MessagingService.this);

                    app.init();

                    return null;
                }

                @Override
                protected void onException(Exception e) {
                    Log.d(TAG, "Failed to initialize app. Server might be unreachable. Will retry.");
                    e.printStackTrace();

                    // Maybe slow down retries..?
                    init();
                }
            };

    private void registerToken(final String token) {
        Log.d(TAG,"About to upload new token.");

        initTask.whenDone(aVoid -> {
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
        });
    }

    private void init() {
        this.initTask.whenDone(
                aVoid -> this.initTask.execute()
        );
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Messaging service startet.");
        eventChannel = new EventChannel(this);

        init();
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        {
            final Gson gson = new Gson();
            Log.d(TAG, "Received FCM message. It contains: " + gson.toJson(remoteMessage.getData()));
        }

        if (remoteMessage.getData().containsKey("event")) {
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
            Log.d(TAG, "Received data notification without event key.");
        }
    }

    @Override
    public void onNewToken(final String token) {
        Log.d(TAG, "Refreshed token: " + token);

        registerToken(token);
    }
}
