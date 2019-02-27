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
        Intent messagingServiceIntent = new Intent(context, MessagingService.class);
        PendingIntent pendingIntent1 = PendingIntent.getService(context,0,messagingServiceIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent appointmentServiceIntent = new Intent(context, ProcessAppointmentRequest.class);
        PendingIntent pendingIntent2 = PendingIntent.getService(context,1,appointmentServiceIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        long startTime = System.currentTimeMillis();
        long intervalTime = 1000;

        AlarmManager alarmManager1 = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager1.setRepeating(AlarmManager.RTC_WAKEUP,startTime,intervalTime,pendingIntent1);

        AlarmManager alarmManager2 = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager2.setRepeating(AlarmManager.RTC_WAKEUP,startTime,intervalTime,pendingIntent2);
    }
}
