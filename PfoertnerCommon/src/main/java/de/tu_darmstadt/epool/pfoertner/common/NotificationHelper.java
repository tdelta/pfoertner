package de.tu_darmstadt.epool.pfoertner.common;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationHelper {
    private static final String ADMIN_PACKAGE = "de.tu_darmstadt.epool.pfoertneradmin";
    private static final String TAG = "Notification";
    private static final String SAVED_NOTIFICATION_ID = "saved notification id";

    /**
     * Notifications need unique ids. This method implements a simple counter.
     *
     * @param context Context needed to access local settings to save the current counter
     * @return A unique id
     */
    private static int getNextId(final Context context){
        final PfoertnerApplication app = PfoertnerApplication.get(context);
        final SharedPreferences settings = app.getSettings();
        final SharedPreferences.Editor editor = settings.edit();

        int id = settings.getInt(SAVED_NOTIFICATION_ID,0);
        id = (id + 1) % Integer.MAX_VALUE;

        editor.putInt(SAVED_NOTIFICATION_ID,id);
        editor.apply();

        return id;
    }

    /**
     * Displays a notification
     *
     * @param notificationContent JSON Object containing the notification. It is structured in the following way:
     *                            {
     *                              title: title,
     *                              body: body,
     *                              buttons: {
     *                                  title: title,
     *                                  intent: url,
 *                                      data: intent extras
     *                              }
     *                            }
     * @param context Context needed to display notifications (e.g. ApplicationContext)
     */
    public static void displayNotification(final String notificationContent, final Context context) {
        try {
            final int notificationId = getNextId(context);
            final JSONObject notificationJson = new JSONObject(notificationContent);
            final String title = notificationJson.getString("title");
            final String body = notificationJson.getString("body");
            final String contentIntentClass = notificationJson.optString("activity", null);

            final String intentPurpose = "AppointmentRequest";

            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), "appointments")
                    .setSmallIcon(R.drawable.ic_tablet_black_24dp)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(0x1111ee)
                    .setVibrate(new long[]{0, 150})
                    .setAutoCancel(true);

            if (contentIntentClass != null) {
                final Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(ADMIN_PACKAGE);
                appIntent.setClassName(ADMIN_PACKAGE, ADMIN_PACKAGE + "." + contentIntentClass);
                appIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                appIntent.putExtra("intentPurpose", intentPurpose);

                final PendingIntent pendingIntent = PendingIntent.getActivity(
                        context.getApplicationContext(),
                        1,
                        appIntent,
                        PendingIntent.FLAG_ONE_SHOT
                );

                notificationBuilder.setContentIntent(pendingIntent);
            }

            //final JSONArray buttonsJson = notificationJson.optJSONArray("buttons");
            //if (buttonsJson != null) {
            //    for (int i = 0; i < buttonsJson.length(); i++) {
            //        final String buttonText = buttonsJson.getJSONObject(i).getString("title");
            //        final String intentUrl = buttonsJson.getJSONObject(i).getString("intent");

            //        final Intent buttonIntent = new Intent(intentUrl);
            //        buttonIntent.putExtra("data", notificationJson.optString("data"));
            //        buttonIntent.putExtra("notificationId", notificationId);

            //        final PendingIntent pendingIntent = PendingIntent.getService(
            //                context.getApplicationContext(),
            //                i,
            //                buttonIntent,
            //                PendingIntent.FLAG_ONE_SHOT);

            //        final NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.common_full_open_on_phone, buttonText, pendingIntent);
            //        notificationBuilder.addAction(action);
            //    }
            //}

            final NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify("de.tu_darmstadt.epool.pfoertner",notificationId, notificationBuilder.build());
        }

        catch (final JSONException e) {
            Log.d(TAG, "Could not parse JSON payload of notification", e);
        }
    }
}
