package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.ArraySet;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class StatusFragment extends DialogFragment{
    public int selected;
    private int tempSelected;
    private List<String> status;
    private SharedPreferences settings;
    TextView textfield;


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

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select Status")
                    .setSingleChoiceItems(status.toArray(new String[0]), selected,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            tempSelected = which;
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
                    });
            return builder.create();
        }

}
