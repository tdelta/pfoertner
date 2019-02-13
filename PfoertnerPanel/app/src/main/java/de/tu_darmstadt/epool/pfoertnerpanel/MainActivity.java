package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.v4.app.FragmentTransaction;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.CalendarApi;
import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.OfficeObserver;
import de.tu_darmstadt.epool.pfoertnerpanel.member.MemberListFragment;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private LayoutInflater inflater;
    private ViewGroup container;
    private MemberListFragment memberList;

    private void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        new RequestTask<Void>() {
            @Override
            protected Void doRequests() {
                app.init();

                return null;
            }

            @Override
            protected void onSuccess(Void result) {
                MainActivity.this.startService(
                        new Intent(MainActivity.this, SyncService.class)
                );

                initOffice();
            }

            @Override
            protected void onException(Exception e) {
                ErrorInfoDialog.show(MainActivity.this, e.getMessage(), aVoid -> init());
            }
        }.execute();

        memberList = new MemberListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.member_list, memberList);
        transaction.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0) {
            onOfficeInitialized();
        }
    }

    private void updateMembers() {
        final PfoertnerApplication app = PfoertnerApplication.get(MainActivity.this);
        memberList.setMembers(app.getOffice().getMembers());

    }

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

    private void onOfficeInitialized() {
        Log.d(TAG, "Office has been initialized.");
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        setGlobalStatus(app.getOffice().getStatus());
        updateMembers();

        registerForMemberChanges(app.getOffice().getMembers());

        app.getOffice().addObserver(new OfficeObserver() {
            @Override
            public void onStatusChanged(final String newStatus) {
                setGlobalStatus(newStatus);
            }

            @Override
            public void onMembersChanged(final List<Member> newMembers, final List<Integer> removedMemberIds) {
                Log.d(TAG, "Members changed, we got " + newMembers.size() + " new member(s) and " + removedMemberIds.size() + " removed member(s).");

                registerForMemberChanges(newMembers);

                updateMembers();
                // TODO: Members einzaln updaten
            }
        });
    }

    public void test(View view){
        Intent intent = new Intent(this, ScheduleAppointment.class);
        intent.putExtra("MemberId",memberList.getCurrentMember());
        startActivity(intent);
    }

    private void registerForMemberChanges(final List<Member> members) {
        final MemberObserver observer = new MemberObserver() {
            @Override
            public void onFirstNameChanged(String newFirstName) {
                updateMembers();
            }

            @Override
            public void onLastNameChanged(String newLastName) {
                updateMembers();
            }

            @Override
            public void onStatusChanged(String newStatus) {
                updateMembers();
            }

            @Override
            public void onPictureChanged() {
                updateMembers();
            }
        };

        for (final Member member : members) {
            Log.d(TAG, "Registering MainActivity observer and CalenderApi on member with id " + member.getId());

            member.addObserver(observer);

            member.setCalendarApi(new CalendarApi(member,this));
        }
    }

    private void checkForPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        if (googleApiAvailability.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            googleApiAvailability.makeGooglePlayServicesAvailable(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity created.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForPlayServices();

        inflater =  getLayoutInflater();
        container = findViewById(R.id.member_list);

        setRoom("S101/A1");
        setGlobalStatus("Extended Access");

        if (savedInstanceState == null) {
            init();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkForPlayServices();
    }

    public void setRoom(final String str){
        TextView room = findViewById(R.id.room);
        room.setText(str);
    }

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
                    toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.pfoertner_negative_status_bg));
                }
            }
        }

        else {
            // TODO: Remove status, if none set?
        }
    }
}
