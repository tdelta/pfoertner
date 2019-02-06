package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.DialogFragment;

public abstract class AbstractStatusFragment extends DialogFragment {
    protected StatusDialogListener listener;


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. */
    public interface StatusDialogListener {
        void startGlobalTextInput();
        void startPersonalTextInput();
    }

    abstract public void setArguments(Activity activity);

    abstract public void updateStatus(String text);

    //TODO: ueberschreiben? oncreatedialog

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (StatusDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    abstract protected void sendStatus(String status);

}
