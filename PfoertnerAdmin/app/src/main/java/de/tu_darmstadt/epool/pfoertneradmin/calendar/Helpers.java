package de.tu_darmstadt.epool.pfoertneradmin.calendar;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.CalendarContract;

public class Helpers {

    /**
     * This function sends a request to synchronize the local google
     * calendar with the online google calendar
     *
     * @param context of the given activity/fragment
     * @param accountName of the account, which will be synchronized
     */
    public static void requestCalendarsSync(final Context context, final String accountName) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(
                new Account(accountName, "com.google"),
                CalendarContract.AUTHORITY,
                bundle
        );
    }
}
