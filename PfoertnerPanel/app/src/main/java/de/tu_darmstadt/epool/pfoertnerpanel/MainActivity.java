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

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Person;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.observers.OfficeObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private EventChannel eventChannel;

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
        this.eventChannel.listen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.eventChannel.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0) {
            final PfoertnerApplication app = PfoertnerApplication.get(this);
            updateMembers();
            setGlobalStatus(app.getOffice().getStatus());
        }
    }

    private void updateMembers() {
        new RequestTask<Person[]>() {
            @Override
            protected Person[] doRequests() {
               final PfoertnerApplication app = PfoertnerApplication.get(MainActivity.this);
               Person[] officeMembers;
               try {
                   officeMembers = app.getService().getOfficeMembers(
                           app.getAuthentication().id,
                           app.getOffice().getId())
                           .execute()
                           .body();
               }

               catch (final IOException e) {
                   e.printStackTrace();

                   officeMembers = null;
               }

               if (officeMembers == null) {
                   throw new RuntimeException("Could not load members of the office. Do you have an internet connection?");
               }

               return officeMembers;
            }

            @Override
            protected void onException(Exception e) {
                ErrorInfoDialog.show(MainActivity.this, e.getMessage(), aVoid -> MainActivity.this.updateMembers());
            }

            @Override
            protected void onSuccess(final Person[] result) {
                // Clear already added members
                removeMembers();

                for (final Person p : result){
                    MainActivity.this.addMember(p);
                }
            }
        }.execute();
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
            new RequestTask<Office>() {
                @Override
                protected Office doRequests() {

                    final Office office = Office.loadOffice(
                            app.getSettings(),
                            app.getService(),
                            app.getAuthentication()
                    );

                    return office;
                }

                @Override
                protected void onException(Exception e){
                    ErrorInfoDialog.show(MainActivity.this, e.getMessage(), aVoid -> initOffice());
                }

                @Override
                protected void onSuccess(Office office){
                    app.setOffice(office);

                    setGlobalStatus(office.getStatus());
                    updateMembers();

                    app.getOffice().addObserver(new OfficeObserver() {
                        @Override
                        public void onStatusChanged(final String newStatus) {
                            setGlobalStatus(newStatus);
                        }
                    });
                }
            }.execute();
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

        eventChannel = new EventChannel(MainActivity.this) {
            @Override
            protected void onEvent(EventType e) {
                switch (e) {
                    case AdminJoined:
                        MainActivity.this.updateMembers();
                        break;
                }
            }
        };

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

    public void setRoom(String str){
        TextView room = findViewById(R.id.room);
        room.setText(str);
    }

    public void setGlobalStatus(String status){
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
        addMember(new Person(-1, "Mustermann", "Prof. Dr. Ing. Max"));
    }

    public void removeMembers(){
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        memberCount=0;

    }

    public void addMember(final Person p){
        // set the attributes of the member to add
        String[] work = {"Mo-Fr 8:00 - 23:00", "Sa-So 8:00 - 23:00"};
        Member member = new Member();

        member.setName(p.firstName + " " + p.lastName);
        member.setStatus(Member.Status.OUT_OF_OFFICE);
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
