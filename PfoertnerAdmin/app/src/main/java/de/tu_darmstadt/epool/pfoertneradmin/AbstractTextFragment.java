package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public abstract class AbstractTextFragment extends DialogFragment {
    protected TextDialogListener listener;
    protected String text = "";
    protected EditText input;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. */
    public interface TextDialogListener {
        public void updateGlobalStatus(String text);
        public void updatePersonalStatus(String text);
    }

    abstract public Dialog onCreateDialog(Bundle savedInstanceState);

    @Override
    public void onCreate(Bundle outState) {
        super.onCreate(outState);
        if (outState != null)
            text = outState.getString("statusName", "");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("was here");
        outState.putString("statusName", input.getText().toString());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (TextDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
