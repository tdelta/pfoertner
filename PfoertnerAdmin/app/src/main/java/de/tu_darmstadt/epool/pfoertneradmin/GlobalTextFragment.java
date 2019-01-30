package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public class GlobalTextFragment extends AbstractTextFragment {




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
                            listener.updateGlobalStatus(text);
                        }
                    });

            return enter_name.create();

    }




    }
