package de.tu_darmstadt.epool.pfoertneradmin.calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Date;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;

public class LocalCalendar {

    private static String TAG = "Calendar";
    public static String CALENDAR_ID = "local calendar id";

    private static LocalCalendar instance;

    private String id;
    private ContentResolver cr;

    public static LocalCalendar getInstance(Context context){
        if(instance == null){
            instance = new LocalCalendar(context);
        }
        return instance;
    }

    public LocalCalendar(Context context){
        PfoertnerApplication app = PfoertnerApplication.get(context);
        if(!app.getSettings().contains(CALENDAR_ID)) {
            Uri localCalendar = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "Pfoertner")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

            ContentValues values = new ContentValues();
            values.put(CalendarContract.Calendars.ACCOUNT_NAME, "Pfoertner");
            values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(CalendarContract.Calendars.NAME, "Pfoertner Appointments");
            values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Pfoertner Appointments");
            values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0x3355ff);
            values.put(CalendarContract.Calendars.VISIBLE, 1);

            Uri newCalendar = context.getContentResolver().insert(localCalendar,values);
            String calendarId = newCalendar.getPathSegments().get(1);

            SharedPreferences.Editor editor = app.getSettings().edit();
            editor.putString(CALENDAR_ID,calendarId);
            editor.apply();
        }
        this.id = app.getSettings().getString(CALENDAR_ID,"0");
        this.cr = context.getContentResolver();
    }

    public void writeEvent(Date start, Date end) throws SecurityException{
        Log.d(TAG,"Writing to calendar");
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(start);
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(end);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, "Office Appointment");
        values.put(CalendarContract.Events.EVENT_TIMEZONE,"Europe/Berlin");
        values.put(CalendarContract.Events.CALENDAR_ID, id);
        cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }
}
