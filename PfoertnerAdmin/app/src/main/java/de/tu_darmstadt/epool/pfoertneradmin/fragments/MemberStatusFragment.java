package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.MemberStatusFragmentViewModel;

public class MemberStatusFragment extends Fragment {
    private static final String TAG = "MemberStatusFragment";

    private MemberStatusFragmentViewModel viewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        final View mainView = inflater.inflate(R.layout.member_status_view, container, false);

        mainView
                .findViewById(R.id.member_status_view_clickable)
                .setOnClickListener(
                        v -> triggerStatusSelection()
                );

        return mainView;
    }

    private void triggerStatusCreation() {
        final AlertDialog enterNewStatus;
        {
            final EditText input = new EditText(getActivity());
            input.setText("");

            AlertDialog.Builder enterNewStatusBuilder = new AlertDialog.Builder(getActivity());
            enterNewStatusBuilder.setView(input)
                    .setTitle("Enter a new status")
                    .setPositiveButton("OK", (dialog, which) -> {
                        viewModel.addToStatusList(input.getText().toString());
                        triggerStatusSelection();
                    });

            enterNewStatus = enterNewStatusBuilder.create();
        }

        enterNewStatus.show();
    }

    private void triggerStatusSelection() {
        final AlertDialog status_select = new AlertDialog.Builder(getActivity())
                .setTitle("Select your Status")
                .setSingleChoiceItems(viewModel.getStatusList().toArray(new String[0]), viewModel.getNewIdx(), (dialog, which) -> viewModel.setNewIdx(which))
                .setNeutralButton("Create New", (dialog, which) -> triggerStatusCreation())
                .setPositiveButton( "OK", (dialog, whichButton) -> {
                    viewModel.setStatus();
                })
                .create();

        status_select.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AdminApplication app = AdminApplication.get(getActivity());

        viewModel = ViewModelProviders.of(this).get(MemberStatusFragmentViewModel.class);
        viewModel.init(app.getMemberId());

        viewModel.getCurrentMemberStatusIdx().observe(this, newSelected -> {
            final String currentStatus;
            if (newSelected != null) {
                viewModel.setNewIdx(newSelected);
                currentStatus = viewModel.getSelectedStatus();
            }

            else {
                viewModel.setNewIdx(0);
                currentStatus = "Available";
            }

            displayStatus(currentStatus);
        });
    }

    public void displayStatus(final String status) {
        final String statusText;
        final Drawable statusIcon;
        final int bgColor;
        final int tapTextColor;

        final Activity activity = getActivity();

        try {
            switch (status) {
                case "Available":
                    statusText = "You're available";
                    statusIcon = ContextCompat.getDrawable(activity, R.drawable.ic_home_green_60dp);
                    bgColor = ContextCompat.getColor(activity, R.color.pfoertner_positive_status_bg);
                    tapTextColor = ContextCompat.getColor(activity, R.color.pfoertner_positive_status_taptext);
                    break;

                case "In meeting":
                case "Out of office":
                    statusText = status;
                    statusIcon = ContextCompat.getDrawable(activity, R.drawable.ic_warning_red_60dp);
                    bgColor = ContextCompat.getColor(activity, R.color.pfoertner_negative_status_bg);
                    tapTextColor = ContextCompat.getColor(activity, R.color.pfoertner_negative_status_taptext);
                    break;

                default:
                    statusText = status;
                    statusIcon = ContextCompat.getDrawable(activity, R.drawable.ic_info_yellow_24dp);
                    bgColor = ContextCompat.getColor(activity, R.color.pfoertner_info_status_bg);
                    tapTextColor = ContextCompat.getColor(activity, R.color.pfoertner_info_status_taptext);
            }

            final TextView memberStatusText = (TextView) activity.findViewById(R.id.member_status_text);
            final ImageView memberStatusIcon = (ImageView) activity.findViewById(R.id.member_status_icon);
            final LinearLayout memberStatusLayout = (LinearLayout) activity.findViewById(R.id.member_status_view_clickable);
            final TextView memberStatusTapText = (TextView) activity.findViewById(R.id.member_status_tap_text);

            memberStatusText.setText(statusText);
            memberStatusIcon.setImageDrawable(statusIcon);
            memberStatusLayout.setBackgroundColor(bgColor);
            memberStatusTapText.setTextColor(tapTextColor);
        }

        catch (final Exception exception) {
            Log.e(TAG, "Could not load resources needed to display current member status.", exception);
        }
    }
}
