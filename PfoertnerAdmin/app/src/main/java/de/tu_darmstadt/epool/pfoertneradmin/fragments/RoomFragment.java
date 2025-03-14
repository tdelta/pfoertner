package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.RoomFragmentViewModel;

public class RoomFragment extends Fragment {
    private static final String TAG = "GlobalStatusFragment";

    private RoomFragmentViewModel viewModel;


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
        final View mainView = inflater.inflate(R.layout.room, container, false);

        mainView.setOnClickListener(
                        v -> triggerRoomCreation(mainView)
                );

        return mainView;
    }

    /**
     * This method triggers the custom room creation.
     *
     *
     * @param mainView view which will be pressed to call this function
     */
    private void triggerRoomCreation(final View mainView) {
        final AlertDialog enterNewStatus;
        {
            final EditText input = new EditText(getActivity());
            final TextView roomInput = mainView.findViewById(R.id.summary);

            input.setText(roomInput.getText().toString());

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

    /**
     *
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PfoertnerApplication app = PfoertnerApplication.get(getActivity());

        viewModel = new ViewModelProvider(this).get(RoomFragmentViewModel.class);
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
