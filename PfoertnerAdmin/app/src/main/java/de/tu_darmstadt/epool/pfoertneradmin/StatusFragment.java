package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusFragment extends DialogFragment {
    private static int selected;
    private static int tempSelected;
    private static List<String> status;
    private static SharedPreferences settings;
    private static TextView textfield;
    private StatusDialogListener listener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. */
    public interface StatusDialogListener {
        public void startTextInput();
    }

        public static StatusFragment newInstance(Activity activity){
            StatusFragment fragment = new StatusFragment();
            fragment.setArguments(activity);
            return fragment;
        }

        public void setArguments(Activity activity){
            settings = activity.getPreferences( 0);
            textfield = activity.findViewById(R.id.summary);
            selected = settings.getInt("globalStatusSelected", 0);

            if (settings.contains("globalStatusModes")){
                Gson gson = new Gson();
                String statusJSON =  settings.getString("globalStatusModes", null);
                status = Arrays.asList(gson.fromJson(statusJSON, String[].class));
            }
            else{
                status = new ArrayList<String>();
                status.add("Do Not Disturb!");
                status.add("Come In!");
                status.add("Only Urgent Matters!");
            }

            textfield.setText("Current: " + status.get(selected));
        }

        public void updateStatus(String text){
            System.out.println(text.getClass() + " | " + text + "|");
            if (!text.trim().equals("") && !status.contains(text.trim())) {
                status.add(text.trim());
                Gson gson = new Gson();
                final SharedPreferences.Editor e = settings.edit();
                e.putString("globalStatusModes", gson.toJson(status));
                e.apply();
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog status_select = new AlertDialog.Builder(getActivity())
                    .setTitle("Select Status")
                    .setSingleChoiceItems(status.toArray(new String[0]), selected,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            tempSelected = which;
                        }
                    })
                    .setNeutralButton("Create New", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            listener.startTextInput();
                        }
                    })
                    .setPositiveButton( "OK", new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton)
                        {
                            selected = tempSelected;
                            final SharedPreferences.Editor e = settings.edit();
                            e.putInt("globalStatusSelected", selected);
                            textfield.setText("Current: " + status.get(selected));
                            e.apply();


                        }
                    })
                    .create();


            return status_select;
        }

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

}
