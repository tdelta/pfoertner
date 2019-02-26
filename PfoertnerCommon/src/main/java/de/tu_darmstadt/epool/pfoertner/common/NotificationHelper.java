package de.tu_darmstadt.epool.pfoertner.common;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import PfoertnerCommon.R;

public class NotificationHelper {

    private static final String ADMIN_PACKAGE = "de.tu_darmstadt.epool.pfoertneradmin";
    private static final String TAG = "Notification";
    private static final String SAVED_NOTIFICATION_ID = "saved notification id";

    private static int getNextId(Context context){
        PfoertnerApplication app = PfoertnerApplication.get(context);
        SharedPreferences settings = app.getSettings();
        SharedPreferences.Editor editor = settings.edit();
        int id = settings.getInt(SAVED_NOTIFICATION_ID,0);
        id = (id + 1) % Integer.MAX_VALUE;
        editor.putInt(SAVED_NOTIFICATION_ID,id);
        editor.apply();
        return id;
    }

    public static void displayNotification(String notificationContent, Context context) {
        try {
            JSONObject notificationJson = new JSONObject(notificationContent);
            String title = notificationJson.getString("title");
            String body = notificationJson.getString("body");
            String contentIntentClass = notificationJson.optString("intent", null);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "appointments")
                    .setSmallIcon(R.drawable.common_full_open_on_phone)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(0x1111ee)
                    .setAutoCancel(true);

            if (contentIntentClass != null) {
                Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(ADMIN_PACKAGE);
                appIntent.setClassName(ADMIN_PACKAGE, ADMIN_PACKAGE + "." + contentIntentClass);
                appIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context.getApplicationContext(),
                        1,
                        appIntent,
                        PendingIntent.FLAG_ONE_SHOT);
                notificationBuilder.setContentIntent(pendingIntent);
            }

            JSONArray buttonsJson = notificationJson.optJSONArray("buttons");
            if (buttonsJson != null) {
                for (int i = 0; i < buttonsJson.length(); i++) {
                    String buttonText = buttonsJson.getJSONObject(i).getString("title");
                    String intentUrl = buttonsJson.getJSONObject(i).getString("intent");

                    Intent buttonIntent = new Intent(intentUrl);
                    buttonIntent.putExtra("data", notificationJson.optString("data"));
                    PendingIntent pendingIntent = PendingIntent.getService(
                            context.getApplicationContext(),
                            i,
                            buttonIntent,
                            PendingIntent.FLAG_ONE_SHOT);
                    NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.common_full_open_on_phone, buttonText, pendingIntent);
                    notificationBuilder.addAction(action);
                }
            }

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify("de.tu_darmstadt.epool.pfoertner",getNextId(context), notificationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Could not parse JSON payload of notification");
        }
    }
}
