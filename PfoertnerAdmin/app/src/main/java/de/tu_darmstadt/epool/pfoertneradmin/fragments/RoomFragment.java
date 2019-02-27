package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.RoomFragmentViewModel;

public class RoomFragment extends Fragment {
    private static final String TAG = "GlobalStatusFragment";

    private RoomFragmentViewModel viewModel;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        final View mainView = inflater.inflate(R.layout.room, container, false);

        mainView.setOnClickListener(
                        v -> triggerRoomCreation()
                );

        return mainView;
    }

    private void triggerRoomCreation() {
        final AlertDialog enterNewStatus;
        {
            final EditText input = new EditText(getActivity());
            input.setText("");

            AlertDialog.Builder enterNewStatusBuilder = new AlertDialog.Builder(getActivity());
            enterNewStatusBuilder.setView(input)
                    .setTitle("Enter a new Room Name")
                    .setPositiveButton("OK", (dialog, which) -> {
                        viewModel.setRoom(input.getText().toString());
                    });

            enterNewStatus = enterNewStatusBuilder.create();
        }
        enterNewStatus.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PfoertnerApplication app = PfoertnerApplication.get(getActivity());

        viewModel = ViewModelProviders.of(this).get(RoomFragmentViewModel.class);
        viewModel.init(app.getOffice().getId());

        viewModel.getCurrentRoomListener().observe(this, newRoom -> {
            final String currentRoom;
            if (newRoom != null) {
                currentRoom = newRoom;
            }
            else {
                currentRoom = "Not Set";
            }
            final TextView textfield = getView().findViewById(R.id.summary);
            textfield.setText(currentRoom);
        });
    }
}
