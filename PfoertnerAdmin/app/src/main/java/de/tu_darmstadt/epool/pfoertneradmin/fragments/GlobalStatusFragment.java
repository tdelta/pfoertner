package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.GlobalStatusFragmentViewModel;

public class GlobalStatusFragment extends Fragment {
    private static final String TAG = "GlobalStatusFragment";

    private GlobalStatusFragmentViewModel viewModel;

    /**
     *
     * @param inflater needed to create views in the fragment
     * @param container parent view of the fragment
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     * @return view for layout context
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        final View mainView = inflater.inflate(R.layout.global_status_view, container, false);

        mainView
                .findViewById(R.id.global_status_view_clickable)
                .setOnClickListener(
                        v -> triggerStatusSelection()
                );

        return mainView;
    }

    /**
     *
     * This method is called to trigger the status creation
     * for the office.
     *
     */
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

    /**
     *
     * This method is called to trigger the status selection
     * for the office.
     *
     */
    private void triggerStatusSelection() {
        final AlertDialog status_select = new AlertDialog.Builder(getActivity())
                .setTitle("Select Global Status")
                .setSingleChoiceItems(viewModel.getStatusList().toArray(new String[0]), viewModel.getNewIdx(), (dialog, which) -> viewModel.setNewIdx(which))
                .setNeutralButton("Create New", (dialog, which) -> triggerStatusCreation())
                .setPositiveButton( "OK", (dialog, whichButton) -> {
                    viewModel.setStatus();
                })
                .create();

        status_select.show();
    }

    /**
     *
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PfoertnerApplication app = PfoertnerApplication.get(getActivity());

        viewModel = new ViewModelProvider(this).get(GlobalStatusFragmentViewModel.class);
        viewModel.init(app.getOffice().getId());

        viewModel.getCurrentOfficeStatusIdx().observe(this, newSelected -> {
            final String currentStatus;
            if (newSelected != null) {
                viewModel.setNewIdx(newSelected);
                currentStatus = viewModel.getSelectedStatus();
            }

            else {
                Log.d(TAG, "There is no status selected. Falling back to default text.");
                viewModel.setNewIdx(0);
                currentStatus = "None selected currently.";
            }

            final TextView textfield = getView().findViewById(R.id.summary);
            final ImageView iconview = getView().findViewById(R.id.globalStatusIcon);

            textfield.setText(currentStatus);
            iconview.setImageDrawable(selectIcon(currentStatus));
        });
    }

    /**
     * Choose status symbol according to given string
     * @param status status given as String
     * @return Drawable chosen symbol
     */
    private Drawable selectIcon(final String status) {
        final Activity activity = getActivity();

        switch(status) {
            case "Come In!":
                return ContextCompat.getDrawable(activity, R.drawable.ic_thumb_up_green_24dp);
            case "Only Urgent Matters!":
            case "Do Not Disturb!":
                return ContextCompat.getDrawable(activity, R.drawable.ic_warning_red_24dp);
            default:
                return ContextCompat.getDrawable(activity, R.drawable.ic_info_yellow_24dp);
        }
    }
}
