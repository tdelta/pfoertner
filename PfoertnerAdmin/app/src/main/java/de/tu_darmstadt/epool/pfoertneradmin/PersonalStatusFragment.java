package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class PersonalStatusFragment extends AbstractStatusFragment {
    private int selected;
    private int tempSelected;
    private List<String> status;
    private AdminApplication app;
    private TextView textfield;

    public static PersonalStatusFragment newInstance(Activity activity){
        PersonalStatusFragment fragment = new PersonalStatusFragment();
        fragment.setArguments(activity);
        return fragment;
    }


    @Override
    public void setArguments(Activity activity) {
        textfield = activity.findViewById(R.id.summary2);

        app = AdminApplication.get(activity);

        final SharedPreferences settings = app.getSettings();

        if (settings.contains("peronalStatusModes")){
            Gson gson = new Gson();
            String statusJSON =  settings.getString("peronalStatusModes", null);
            status = Arrays.asList(gson.fromJson(statusJSON, String[].class));
        }

        else{
            status = new ArrayList<>();
            status.add("Out of office");
            status.add("In meeting");
            status.add("Available");
        }

        textfield.setText("No special Status set");
    }

    @Override
    public void updateStatus(String text) {
        System.out.println(text.getClass() + " | " + text + "|");

        if (!text.trim().equals("") && !status.contains(text.trim())) {
            status.add(text.trim());
            Gson gson = new Gson();
            final SharedPreferences.Editor e = app.getSettings().edit();
            e.putString("peronalStatusModes", gson.toJson(status));
            e.apply();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog status_select = new AlertDialog.Builder(getActivity())
                .setTitle("Select personal Status")
                .setSingleChoiceItems(status.toArray(new String[0]), selected, (dialog, which) -> tempSelected = which)
                .setNeutralButton("Create New", (dialog, which) -> listener.startPersonalTextInput())
                .setPositiveButton( "OK", (dialog, whichButton) -> {
                    selected = tempSelected;

                    final String statusText = status.get(selected);

                    sendStatus(statusText);
                })
                .create();

        return status_select;
    }

    @Override
    protected void sendStatus(String status) {
        textfield.setText(status);

        final Optional<Member> maybeMember = app.getOffice().getMemberById(app.getMemberId());

        maybeMember.ifPresent(
                member -> member.setStatus(
                        app.getService(),
                        app.getAuthentication(),
                        status
                )
        );
    }
}
