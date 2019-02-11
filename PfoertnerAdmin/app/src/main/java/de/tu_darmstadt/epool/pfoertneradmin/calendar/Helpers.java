package de.tu_darmstadt.epool.pfoertneradmin.calendar;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.CalendarContract;

public class Helpers {
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
