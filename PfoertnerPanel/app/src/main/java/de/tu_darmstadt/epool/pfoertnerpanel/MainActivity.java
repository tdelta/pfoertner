package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.arch.lifecycle.ViewModelProviders;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;


import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.helpers.AtheneReader;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import de.tu_darmstadt.epool.pfoertnerpanel.services.MemberCalendarInfoService;
import de.tu_darmstadt.epool.pfoertnerpanel.member.MemberButton;
import de.tu_darmstadt.epool.pfoertnerpanel.member.MemberGrid;
import de.tu_darmstadt.epool.pfoertnerpanel.viewmodels.OfficeViewModel;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

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
                            () -> {
                                MainActivity.this.startService(
                                        new Intent(MainActivity.this, MemberCalendarInfoService.class)
                                );

                                initOffice();
                            },
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

        viewModel = ViewModelProviders.of(this).get(OfficeViewModel.class);
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
        OfficeViewModel viewModel = ViewModelProviders.of(this).get(OfficeViewModel.class);

        MemberGrid memberList = findViewById(R.id.member_list);
        final PfoertnerApplication app = PfoertnerApplication.get(MainActivity.this);
        viewModel.getOfficeMembers(app.getOffice().getId()).observe(this, members -> {
            if(members != null) {
                memberList.setMembers(members);

                members
                        .stream()
                        .forEach(member -> setTimeEvents(member, memberList));

            }
        });
    }

    /**
     * Observe the calendar events for a member and forward the events to
     * the gridview
     * @param member the member whose calendar should be observed
     * @param memberList the griview for members
     */
    private void setTimeEvents(Member member, MemberGrid memberList) {
        final PanelApplication app = PanelApplication.get(this);
        app
                .getPanelRepo()
                .getMemberCalendarInfoRepo()
                .getCalendarInfoByMemberId(member.getId())
                .observe(this, calendarInfo -> {
                    if (calendarInfo != null && calendarInfo.getCalendarId() != null) {
                        final DateTime start = new DateTime(System.currentTimeMillis());
                        final DateTime end = new DateTime(System.currentTimeMillis() + 86400000L *28);

                        disposables.add(
                        getEvents(calendarInfo, start, end)
                                .subscribe(
                                        events -> {

                                            Log.d("OfficeHours received", "");

                                            memberList.setEvents(member.getId(), events);

                                        },
                                        throwable -> Log.e(TAG, "Failed to fetch events.", throwable)
                                )
                        );
                    }});
    }


    /**
     * get an observable for calendarEvents given a calendar
     * @param calendarInfo the calendar
     * @param start the start limit for returned events
     * @param end the end limit for returned events
     * @return an observable which delivers all events on a change
     */
    private Observable<List<Event>> getEvents(final MemberCalendarInfo calendarInfo, final DateTime start, final DateTime end) {
        final PanelApplication app = PanelApplication.get(this);

        return app
                .getCalendarApi()
                .getCredential(calendarInfo.getOAuthToken())
                .flatMapObservable(
                        credentials -> app
                                .getCalendarApi()
                                .getEvents(calendarInfo.getCalendarId(), credentials, start, end)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
