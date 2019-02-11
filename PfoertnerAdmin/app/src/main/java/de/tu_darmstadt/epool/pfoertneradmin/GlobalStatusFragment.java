package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.OfficeObserver;

public class GlobalStatusFragment extends AbstractStatusFragment {
    private int selected;
    private int tempSelected;
    private List<String> status;
    private PfoertnerApplication app;
    private TextView textfield;
    private ImageView iconview;
    private Activity activity;

    public static GlobalStatusFragment newInstance(Activity activity){
        GlobalStatusFragment fragment = new GlobalStatusFragment();
        fragment.setArguments(activity);
        return fragment;
    }


    public void setArguments(Activity activity){
        this.activity = activity;
        textfield = activity.findViewById(R.id.summary);
        iconview = activity.findViewById(R.id.globalStatusIcon);

        app = PfoertnerApplication.get(activity);

        final SharedPreferences settings = app.getSettings();

        if (settings.contains("globalStatusModes")){
            Gson gson = new Gson();
            String statusJSON =  settings.getString("globalStatusModes", null);
            status = Arrays.asList(gson.fromJson(statusJSON, String[].class));
        }

        else{
            status = new ArrayList<>();
            status.add("Do Not Disturb!");
            status.add("Come In!");
            status.add("Only Urgent Matters!");
        }

        app.getOffice().addObserver(
                new OfficeObserver() {
                    @Override
                    public void onStatusChanged(final String newStatus) {
                        final int selected = status.indexOf(newStatus);

                        if (selected >= 0) {
                            final SharedPreferences.Editor e = app.getSettings().edit();

                            e.putInt("globalStatusSelected", selected);
                            e.apply();
                        }

                        textfield.setText(newStatus);
                        iconview.setImageDrawable(selectIcon(newStatus));
                    }
                }
        );

        final String currentStatus = app.getOffice().getStatus();
        if (currentStatus == null) {
            textfield.setText("None selected currently.");
        }

        else {
            final int i = status.indexOf(currentStatus);
            if (i >= 0) {
                selected = i;
            }

            else {
                selected = 0;
            }

            textfield.setText(currentStatus);
        }

        iconview.setImageDrawable(selectIcon(currentStatus));
    }

    private Drawable selectIcon(final String status) {
        if (status == null || status.equals("Come In!")) {
            return ContextCompat.getDrawable(activity, R.drawable.ic_thumb_up_green_24dp);
        }

        else {
            return ContextCompat.getDrawable(activity, R.drawable.ic_warning_red_24dp);
        }
    }

    public void updateStatus(String text){
        System.out.println(text.getClass() + " | " + text + "|");

        if (!text.trim().equals("") && !status.contains(text.trim())) {
            status.add(text.trim());
            Gson gson = new Gson();
            final SharedPreferences.Editor e = app.getSettings().edit();
            e.putString("globalStatusModes", gson.toJson(status));
            e.apply();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog status_select = new AlertDialog.Builder(getActivity())
                .setTitle("Select Global Status")
                .setSingleChoiceItems(status.toArray(new String[0]), selected, (dialog, which) -> tempSelected = which)
                .setNeutralButton("Create New", (dialog, which) -> listener.startGlobalTextInput())
                .setPositiveButton( "OK", (dialog, whichButton) -> {
                    selected = tempSelected;

                    final String statusText = status.get(selected);

                    sendStatus(statusText);
                })
                .create();

        return status_select;
    }



    protected void sendStatus(String status){
        final Office office = app.getOffice();

        office.setStatus(
                app.getService(),
                app.getAuthentication(),
                status
        );
    }
}
