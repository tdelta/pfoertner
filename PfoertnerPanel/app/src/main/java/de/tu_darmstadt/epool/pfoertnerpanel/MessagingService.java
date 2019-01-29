package de.tu_darmstadt.epool.pfoertnerpanel;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
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
                        e.printStackTrace();

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
        eventChannel = new EventChannel(this);

        init();
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        Log.d(TAG, "Received FCM message.");

        if (remoteMessage.getData().containsKey("event")) {
            try {
                eventChannel.send(
                        EventChannel.EventType.valueOf(
                                remoteMessage.getData().get("event")
                        )
                );
            }

            catch (final IllegalArgumentException e) {
                e.printStackTrace();

                Log.d(TAG, "Server did send unknown event: " + remoteMessage.getData().get("event"));
            }
        }

        else if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Received data notification without event key");
        }
    }

    @Override
    public void onNewToken(final String token) {
        Log.d(TAG, "Refreshed token: " + token);

        registerToken(token);
    }
}
