package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.MemberProfileViewModel;

/**
 * First fragment shown in MainActivity. It corresponds to the "Home" entry in
 * the Drawer
 */
public class MainScreenFragment extends Fragment {
    public MainScreenFragment() {

    }

    /**
     *
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * @param inflater needed to create views in the fragment
     * @param container parent view of the fragment
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     * @return view for layout context
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_main_screen, container, false);

        {
            final FragmentManager fragmentManager = getChildFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            final GlobalStatusFragment globalStatusFragment = new GlobalStatusFragment();
            fragmentTransaction.add(R.id.global_status_view, globalStatusFragment);

            final RoomFragment roomFragment = new RoomFragment();
            fragmentTransaction.add(R.id.room_card, roomFragment);

            final MemberStatusFragment memberStatusFragment = new MemberStatusFragment();
            fragmentTransaction.add(R.id.member_status_view, memberStatusFragment);

            fragmentTransaction.commit();
        }

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
