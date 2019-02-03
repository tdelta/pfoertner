package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.OfficeObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;

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

        for (final de.tu_darmstadt.epool.pfoertner.common.synced.Member m : app.getOffice().getMembers()){
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
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        setGlobalStatus(app.getOffice().getStatus());
        updateMembers();

        app.getOffice().addObserver(new OfficeObserver() {
            @Override
            public void onStatusChanged(final String newStatus) {
                setGlobalStatus(newStatus);
            }

            @Override
            public void onMembersChanged() {
                registerForMemberChanges();

                updateMembers();
                // TODO: Members einzaln updaten
            }
        });

        registerForMemberChanges();
    }

    public void test(View view){
        Intent intent = new Intent(this, ScheduleAppointment.class);
        startActivity(intent);
    }

    private void registerForMemberChanges() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

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
        };

        // TODO: Effizienter, nur für einzelne Members
        final List<de.tu_darmstadt.epool.pfoertner.common.synced.Member> members = app.getOffice().getMembers();

        for (final de.tu_darmstadt.epool.pfoertner.common.synced.Member member : members) {
            member.deleteObserver(observer);
            member.addObserver(observer);
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForPlayServices();

        inflater =  getLayoutInflater();
        container = findViewById(R.id.member_insert);

        setRoom("S101/A1");
        setGlobalStatus("Extended Access");

        init();
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
        addMember(new MemberData(-1, "Prof. Dr. Ing. Max", "Mustermann", ""));
    }

    public void removeMembers(){
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        memberCount=0;

    }

    public void addMember(final de.tu_darmstadt.epool.pfoertner.common.synced.Member m) {
        addMember(m.toData());
    }

    public void addMember(final MemberData m){
        // set the attributes of the member to add
        String[] work = {"Mo-Fr 8:00 - 23:00", "Sa-So 8:00 - 23:00"};
        Member member = new Member();

        member.setName(m.firstName + " " + m.lastName);
        member.setStatus(m.status == null ? "" : m.status);
        member.setOfficeHours(work);
        //member.setImage(getDrawable(R.drawable.ic_contact_default));

        switch(memberCount){
            case 0:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_one, member);
                transaction.addToBackStack(null);
                transaction.commit();
                memberCount++;
                break;
            }
            case 1:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_two, member);
                transaction.addToBackStack(null);

                transaction.commit();
                memberCount++;
                break;
            }
            case 2:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_three, member);
                transaction.addToBackStack(null);
                transaction.commit();
                memberCount++;
                break;
            }
            case 3:{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.member_four, member);
                transaction.addToBackStack(null);
                transaction.commit();
                memberCount++;
                break;
            }
        }
    }
}
