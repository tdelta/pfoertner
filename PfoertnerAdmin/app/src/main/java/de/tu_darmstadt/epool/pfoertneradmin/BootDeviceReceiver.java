package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.tu_darmstadt.epool.pfoertner.common.MessagingService;

public class BootDeviceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startIntent = new Intent(context, MessagingService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,0,startIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        long startTime = System.currentTimeMillis();
        long intervalTime = 1000;

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,startTime,intervalTime,pendingIntent);
    }
}
