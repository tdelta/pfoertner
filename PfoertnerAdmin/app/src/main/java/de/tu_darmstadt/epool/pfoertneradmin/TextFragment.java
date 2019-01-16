package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public class TextFragment extends DialogFragment {
    private String text = "";
    private EditText input;
    private TextDialogListener listener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. */
    public interface TextDialogListener {
        public void updateStatus(String text);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

            input = new EditText(getActivity());
            input.setText(text);

            AlertDialog.Builder enter_name = new AlertDialog.Builder((getActivity()));
            enter_name.setView(input)
                    .setTitle("Enter Name")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            text = input.getText().toString();
                            listener.updateStatus(text);
                        }
                    });

            return enter_name.create();

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

    }
