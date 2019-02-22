package de.tu_darmstadt.epool.pfoertner.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
        Log.d(TAG, "Messaging service startet.");
        eventChannel = new EventChannel(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("appointments", "Appointment Requests", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for new appointments requested at the door panel");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        init();
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        {
            final Gson gson = new Gson();
            Log.d(TAG, "Received FCM message. It contains: " + gson.toJson(remoteMessage.getData()));
        }

        if(remoteMessage.getData().containsKey("notification")){
            Log.d(TAG,"Building a notification");
            displayNotification(remoteMessage.getData().get("notification"));
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

    public void displayNotification(String notificationContent){
        try {
            JSONObject notificationJson = new JSONObject(notificationContent);
            String title = notificationJson.getString("title");
            String body  = notificationJson.getString("body");

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),"appointments")
                    .setSmallIcon(R.drawable.common_full_open_on_phone)
                    .setContentTitle(title)
                    .setContentText(body);

            JSONArray buttonsJson = notificationJson.optJSONArray("buttons");
            if(buttonsJson != null){
                for(int i = 0;i < buttonsJson.length();i++){
                    String buttonText = buttonsJson.getJSONObject(i).getString("title");
                    String intentUrl = buttonsJson.getJSONObject(i).getString("intent");

                    Intent buttonIntent = new Intent(intentUrl);
                    buttonIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    buttonIntent.putExtra("data",notificationJson.optString("data"));
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        i,
                        buttonIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                    NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.common_full_open_on_phone, buttonText, pendingIntent);
                    notificationBuilder.addAction(action);
                }
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notificationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG,"Could not parse JSON payload of notification");
        }
    }
}
