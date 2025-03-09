package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import java.time.LocalDateTime;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.helpers.AtheneReader;
import de.tu_darmstadt.epool.pfoertnerpanel.member.MemberButton;
import de.tu_darmstadt.epool.pfoertnerpanel.member.MemberGrid;
import de.tu_darmstadt.epool.pfoertnerpanel.viewmodels.OfficeViewModel;
import io.reactivex.disposables.CompositeDisposable;

/**
 * The main activity, houses the members that are displayed
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = "PfoertnerPanelMain";

    private LayoutInflater inflater;
    private ViewGroup container;
    private CompositeDisposable disposables;

    private OfficeViewModel viewModel;

    private AtheneReader atheneReader;

    private void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        disposables.add(
                app
                        .init()
                        .subscribe(
                                this::initOffice,
                            throwable -> {
                                Log.e(TAG, "Could not initialize. Asking user to retry...", throwable);

                                ErrorInfoDialog.show(MainActivity.this, throwable.getMessage(), aVoid -> init(), false);
                            }
                    )
        );
    }

    /**
     * start the editappointments activity when the nfc chip detects an athene card
     */
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(atheneReader.isTechDiscovered(intent)){
            atheneReader.beep();
            String atheneId = atheneReader.extractAtheneId(intent);
            Intent editAppointmentsActivityIntent = new Intent(this,EditAppointmentsActivity.class);
            editAppointmentsActivityIntent.putExtra("atheneId",atheneId);
            startActivity(editAppointmentsActivityIntent);
        }
    }

    /**
     * destroy the rxJava subscriptions
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposables.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0) {
            onOfficeInitialized();
        }
    }

    /**
     * Initializes the panel on first launch. Shows QR-code
     */
    private void initOffice() {
        final PfoertnerApplication app = PfoertnerApplication.get(MainActivity.this);

        if (!app.getSettings().getBoolean("Initialized", false)) {
            // for now, immediately start initialization screen
            final Intent initIntent = new Intent(
                    MainActivity.this,
                    InitializationActivity.class
            );

            MainActivity.this.startActivityForResult(initIntent, 0);
        }

        else {
            onOfficeInitialized();
        }
    }

    /**
     * Populate various UI fields when the panel is initialized
     */
    private void onOfficeInitialized() {
        Log.d(TAG, "Office has been initialized.");
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        OfficeViewModel viewModel = new ViewModelProvider(this).get(OfficeViewModel.class);

        viewModel.init(app.getOffice().getId());
        viewModel.getOffice().observe(this, office -> {
            if(office != null) {
                setGlobalStatus(office.getStatus());
                setRoom(office.getRoom());
            }
        });
        initializeMemberGrid();
    }

    /**
     * Change active activity to ScheduleAppointmentActivity
     * @param view view context of layout
     */
    public void gotoScheduleAppointment(View view){
        Intent intent = new Intent(this, ScheduleAppointment.class);
        intent.putExtra("MemberId", ((MemberButton) view).getMemberId());
        startActivity(intent);
    }


    /**
     * check if playservices are available and makes them available if they aren't.
     * Used for notifications
     */
    private void checkForPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        if (googleApiAvailability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            googleApiAvailability.makeGooglePlayServicesAvailable(this);
        }
    }

    /**
     * Create the activity and initialize the panel if not already done
     * @param savedInstanceState bundle that contains info from the last activity instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity created.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        atheneReader = new AtheneReader(this);

        checkForPlayServices();

        if (disposables != null) {
            disposables.dispose();
        }

        disposables = new CompositeDisposable();

        inflater = getLayoutInflater();

        if (savedInstanceState == null) {
            init();
        } else {
            setRoom(savedInstanceState.getString("room", null));
            setGlobalStatus(savedInstanceState.getString("globalStatus", null));
            initializeMemberGrid();
        }

    }

    /**
     * Populate the member views generated from member data.
     * Listens for changes.
     */
    private void initializeMemberGrid() {
        OfficeViewModel viewModel = new ViewModelProvider(this).get(OfficeViewModel.class);

        MemberGrid memberList = findViewById(R.id.member_list);
        final PfoertnerApplication app = PfoertnerApplication.get(MainActivity.this);
        viewModel.getOfficeMembers(app.getOffice().getId()).observe(this, members -> {
            if(members != null) {
                memberList.setMembers(members);

                members
                        .stream()
                        .forEach(member -> setTimeslots(member, memberList));

            }
        });
    }

    /**
     * Observe the time slots for a member and forward the time slots to
     * the gridview
     * @param member the member whose calendar should be observed
     * @param memberList the griview for members
     */
    private void setTimeslots(Member member, MemberGrid memberList) {
        final PanelApplication app = PanelApplication.get(this);
        app.getRepo()
                .getTimeslotRepo()
                .getTimeslotsOfMember(LocalDateTime.now().plusDays(7), member.getId())
                .observe(this, timeslots -> {
                    if (timeslots != null) {
                        memberList.setTimeslots(member.getId(), timeslots);
                    }
                });
    }

    /**
     * save various stuff that should be restored on activity recreation
     * @param outState the bundle that survives activity ends
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("room", getRoom());
        outState.putString("globalStatus", getGlobalStatus());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    /**
     * Pause the Athene Card when the main activity is paused
     */
    @Override
    protected void onPause(){
        super.onPause();
        atheneReader.pause();
    }


    /**
     * Resume the Athene Card when the main activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();

        atheneReader.resume();
        checkForPlayServices();
    }

    /**
     * Sets the Room Name in the panel user interface.
     * @param str New name of the Room
     */
    public void setRoom(final String str){
        TextView room = findViewById(R.id.room);
        if (str != null) {
            room.setText(str);
        } else {room.setText("Room Name Not Set");

        }
    }

    /**
     * Returns the current Room Name in the panel user interface.
     * @return String with the current room name
     */
    public String getRoom(){
        TextView room = findViewById(R.id.room);
        return room.getText().toString();
    }

    /**
     * Sets the Global Status in the panel user interface.
     * @param status String with the new global status
     */
    public void setGlobalStatus(final String status){
        if (status != null) {
            TextView global = findViewById(R.id.global_status);
            Toolbar toolbar = findViewById(R.id.toolbar);
            global.setText(status);
            switch (status) {
                case "Come In!": {
                    toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.pfoertner_positive_status_bg));
                    break;
                }
                case "Do Not Disturb!": {
                    toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.pfoertner_negative_status_bg));
                    break;
                }
                case "Extended Access": {
                    toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
                    break;
                }
                default: {
                    toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.pfoertner_info_status_bg));
                }
            }
        }
        else {
            setGlobalStatus("Come In!");
        }
    }

    /**
     * Retuns the current Global Status in the panel user interface.
     * @return String with the current global status
     */
    public String getGlobalStatus() {
        TextView global = findViewById(R.id.global_status);
        return global.getText().toString();
    }

}
