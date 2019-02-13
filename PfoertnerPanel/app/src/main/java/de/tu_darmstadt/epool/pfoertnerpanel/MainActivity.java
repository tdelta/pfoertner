package de.tu_darmstadt.epool.pfoertnerpanel;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.OfficeObserver;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private LayoutInflater inflater;
    private ViewGroup container;

    private int memberCount = 0;

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
        // Clear already added members
        removeMembers();

        final PfoertnerApplication app = PfoertnerApplication.get(MainActivity.this);

        for (final Member m : app.getOffice().getMembers()){
            this.addMember(m);
        }
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

    public void newtest(View view){
        Intent intent = new Intent(this, NewScheduleAppointment.class);
        startActivity(intent);
    }

    public void test(View view){
        Intent intent = new Intent(this, ScheduleAppointment.class);
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

        inflater = getLayoutInflater();
        container = findViewById(R.id.member_insert);

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
            global.setText(status);
            switch (status) {
                case "Come In!": {
                    global.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                    break;
                }
                case "Do Not Disturb!": {
                    global.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                    global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                    break;
                }
                case "Extended Access": {
                    global.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                    global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    break;
                }
                default: {
                    global.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                    global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                }
            }
        }

        else {
            // TODO: Remove status, if none set?
        }
    }

    public void addStdMember(View view) {
        addMember(new MemberData(-1, "Prof. Dr. Ing. Max", "Mustermann", "", "Away"), null);
    }

    public void removeMembers(){
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        memberCount=0;

    }

    public void addMember(final Member m) {
        addMember(m.getMemberData(),m);
    }

    public void addMember(final MemberData memberData, final @Nullable Member member){
        // set the attributes of the member to add
        String[] work = {"Mo-Fr 8:00 - 23:00", "Sa-So 8:00 - 23:00"};
        MemberFragment memberUI = new MemberFragment();

        memberUI.setName(memberData.firstName + " " + memberData.lastName);
        memberUI.setStatus(memberData.status == null ? "" : memberData.status);
        memberUI.setOfficeHours(work);

        if (member != null) {
            final PfoertnerApplication app = PfoertnerApplication.get(this);

            memberUI.setPicture(
                    member
                        .getPicture(app.getFilesDir())
                        .map(bitmap -> (Drawable) new BitmapDrawable(this.getResources(), bitmap))
                        .orElse(
                                getDrawable(R.drawable.ic_contact_default)
                        )
            );
        }
        //member.setPicture(getDrawable(R.drawable.ic_contact_default));

        switch(memberCount){
            case 0:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_one, memberUI);
                transaction.addToBackStack(null);
                transaction.commit();
                memberCount++;
                break;
            }
            case 1:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_two, memberUI);
                transaction.addToBackStack(null);

                transaction.commit();
                memberCount++;
                break;
            }
            case 2:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_three, memberUI);
                transaction.addToBackStack(null);
                transaction.commit();
                memberCount++;
                break;
            }
            case 3:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_four, memberUI);
                transaction.addToBackStack(null);
                transaction.commit();
                memberCount++;
                break;
            }
        }
    }
}
