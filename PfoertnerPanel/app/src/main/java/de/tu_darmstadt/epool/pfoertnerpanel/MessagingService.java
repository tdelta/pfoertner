package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.FcmTokenCreationData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";
    private EventChannel eventChannel;

    private void registerToken(final String token) {
        final Context self = this;

        final SharedPreferences preferences = self.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        final PfoertnerService service = PfoertnerService.makeService();

        Log.d(TAG,"About to upload new token.");
        new RequestTask<Void>() {
            @Override
            protected Void doRequests() {
                final Password pswd = Password.loadPassword(preferences);
                final User device = User.loadDevice(preferences, service, pswd);

                final Authentication authentication = Authentication.authenticate(preferences, service, device, pswd, self);

                try {
                    service.setFcmToken(authentication.id, device.id, new FcmTokenCreationData(token))
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
    }

    @Override
    public void onCreate() {
        eventChannel = new EventChannel(this);
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
