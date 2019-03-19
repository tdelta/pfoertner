package de.tu_darmstadt.epool.pfoertner.common;

import android.app.AlertDialog;
import android.content.Context;

import java.util.function.Consumer;

public class ErrorInfoDialog {

    /**
     * Displays an alert
     *
     * @param context Needed to build an alert in Android. For example Application Context.
     * @param message Message displayed in the alert
     * @param retryFunction Called when the user clicks retry
     * @param cancelable If true, a cancel button will be displayed
     */
    public static void show(
            final Context context,
            final String message,
            final Consumer<Void> retryFunction,
            final boolean cancelable
    ) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setMessage(
                "We encountered a problem:\n\n" +
                        message
        );

        alertDialogBuilder.setPositiveButton(
                "Retry",
                (dialog, which) -> retryFunction.accept(null)
        );

        alertDialogBuilder.setCancelable(cancelable);

        alertDialogBuilder.show();
    }
}
