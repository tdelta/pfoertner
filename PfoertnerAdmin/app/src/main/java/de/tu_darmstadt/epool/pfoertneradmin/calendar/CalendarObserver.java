package de.tu_darmstadt.epool.pfoertneradmin.calendar;

import android.accounts.Account;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CalendarContract;


public class CalendarObserver extends ContentObserver {

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };
    private ContentResolver resolver;


    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public CalendarObserver(Handler handler, ContentResolver resolver) {
        super(handler);
        this.resolver = resolver;
        System.out.println("New observer created");
    }

    /*
     * Define a method that's called when data in the
     * observed content provider changes.
     * This method signature is provided for compatibility with
     * older platforms.
     */
    @Override
    public void onChange(boolean selfChange) {
        /*
         * Invoke the method signature available as of
         * Android platform version 4.1, with a null URI.
         */
        onChange(selfChange, null);
    }

    /*
     * Define a method that's called when data in the
     * observed content provider changes.
     */
    @Override
    public void onChange(boolean selfChange, Uri changeUri) {
        /*
         * Ask the framework to run your sync adapter.
         * To maintain backward compatibility, assume that
         * changeUri is null.
         */
        System.out.println("Change in calendar registered!");
        System.out.println(changeUri);

        Cursor cursor = resolver.query(Uri.parse("content://com.android.calendar/events"),
                EVENT_PROJECTION,
                null,
                null,
                null);


        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
               //TODO: Do something with the events
            }
        }
    }




}

