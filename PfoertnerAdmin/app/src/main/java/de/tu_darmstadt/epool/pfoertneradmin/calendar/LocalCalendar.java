package de.tu_darmstadt.epool.pfoertneradmin.calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.util.Log;

import com.google.android.gms.common.internal.AccountType;

import java.util.Date;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;

public class LocalCalendar {

    public static String CALENDAR_ID = "primary calendar id";
    private long calendarId;
    private String userEmail;

    private static LocalCalendar instance;

    private ContentResolver cr;

    public static LocalCalendar getInstance(Context context, String email){
        if(instance == null){
            instance = new LocalCalendar(context, email);
        }
        instance.setUserEmail(email);
        return instance;
    }

    public void setUserEmail(String userEmail){
        this.userEmail = userEmail;
    }

    public LocalCalendar(Context context, String email) {
        PfoertnerApplication app = PfoertnerApplication.get(context);
        this.cr = context.getContentResolver();

        if (!app.getSettings().contains(CALENDAR_ID)) {
            Uri calendarIdUri = asSyncAdapter(CalendarContract.Calendars.CONTENT_URI,email);

            String[] projection = new String[]{CalendarContract.Calendars._ID};
            String selection = CalendarContract.Calendars.IS_PRIMARY + " = ?";
            String[] selectionArgs = new String[]{"1"};

            Cursor cursor = cr.query(calendarIdUri, projection, selection, selectionArgs, null);
            cursor.moveToFirst();
            calendarId = cursor.getLong(0);

            SharedPreferences.Editor editor = app.getSettings().edit();
            editor.putLong(CALENDAR_ID,calendarId);
            editor.apply();
        } else {
            calendarId = app.getSettings().getLong(CALENDAR_ID,1);
        }
    }

    private Uri asSyncAdapter (Uri uri, String email){
        return uri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, email)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google")
                .build();
    }

    public void writeEvent(Date start, Date end, String attendee, String email, String message) throws SecurityException{
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(start);
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(end);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, "Office Appointment");
        values.put(CalendarContract.Events.DESCRIPTION,message);
        values.put(CalendarContract.Events.EVENT_TIMEZONE,"Europe/Berlin");
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        String eventId = cr.insert(asSyncAdapter(CalendarContract.Events.CONTENT_URI,userEmail), values).getPathSegments().get(1);

        values = new ContentValues();
        values.put(CalendarContract.Attendees.ATTENDEE_NAME,attendee);
        values.put(CalendarContract.Attendees.EVENT_ID,eventId);
        values.put(CalendarContract.Attendees.ATTENDEE_EMAIL,email);
        cr.insert(asSyncAdapter(CalendarContract.Attendees.CONTENT_URI,userEmail),values);
    }
}
