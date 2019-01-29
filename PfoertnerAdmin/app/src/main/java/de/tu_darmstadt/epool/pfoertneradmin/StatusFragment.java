package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.observers.OfficeObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;

public class StatusFragment extends DialogFragment {
    private int selected;
    private int tempSelected;
    private List<String> status;
    private PfoertnerApplication app;
    private TextView textfield;
    private StatusDialogListener listener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks. */
    public interface StatusDialogListener {
        void startTextInput();
    }

    public static StatusFragment newInstance(Activity activity){
        StatusFragment fragment = new StatusFragment();
        fragment.setArguments(activity);
        return fragment;
    }

    public void setArguments(Activity activity){
        textfield = activity.findViewById(R.id.summary);

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

                        textfield.setText("Current: " + newStatus);
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

            textfield.setText("Current: " + currentStatus);
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
                .setTitle("Select Status")
                .setSingleChoiceItems(status.toArray(new String[0]), selected, (dialog, which) -> tempSelected = which)
                .setNeutralButton("Create New", (dialog, which) -> listener.startTextInput())
                .setPositiveButton( "OK", (dialog, whichButton) -> {
                    selected = tempSelected;

                    final String statusText = status.get(selected);

                    sendStatus(statusText);
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

    private void sendStatus(String status){
        final Office office = app.getOffice();

        office.setStatus(
                app.getService(),
                app.getAuthentication(),
                status
        );
    }
}
