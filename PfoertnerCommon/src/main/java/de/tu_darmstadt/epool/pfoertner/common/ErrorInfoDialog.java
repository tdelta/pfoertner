package de.tu_darmstadt.epool.pfoertner.common;

import android.app.AlertDialog;
import android.content.Context;

import java.util.function.Consumer;

public class ErrorInfoDialog {
    public static void show(
            final Context context,
            final String message,
            final Consumer<Void> retryFunction
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

        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.show();
    }
}
