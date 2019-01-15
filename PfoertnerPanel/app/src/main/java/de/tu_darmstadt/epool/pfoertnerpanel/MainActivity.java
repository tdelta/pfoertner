package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Person;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;

import static android.content.Context.MODE_PRIVATE;
import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final MainActivity self = this;

    private LayoutInflater inflater;
    private ViewGroup container;
    private int memberCount = 0;

    private EventChannel eventChannel;

    @Override
    protected void onStart() {
        super.onStart();
        eventChannel.listen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventChannel.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0) {
            updateMembers();
        }
    }

    private void updateMembers() {
        final MainActivity context = this;

        final SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        new RequestTask<Person[]>() {
            @Override
            protected Person[] doRequests() {
               final PfoertnerService service = PfoertnerService.makeService();

               final Password password = Password.loadPassword(preferences);
               final User device = User.loadDevice(preferences, service, password);
               final Authentication auth = Authentication.authenticate(preferences, service, device, password, context);
               final Office office = Office.loadOffice(preferences, service, auth);

               Person[] officeMembers;
               try {
                   officeMembers = service.getOfficeMembers(auth.id, office.id).execute().body();
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
                ErrorInfoDialog.show(context, e.getMessage(), (aVoid) -> context.updateMembers());
            }

            @Override
            protected void onSuccess(final Person[] result) {
                // Clear already added members
                removeMembers();

                for (final Person p : result){
                    context.addMember(p);
                }
            }
        }.execute();
    }

    public enum GlobalStatus {
        DO_NOT_DISTURB, COME_IN, EXTENDED_ACCESS
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

        eventChannel = new EventChannel(this) {
            @Override
            protected void onEvent(EventType e) {
                switch (e) {
                    case AdminJoined:
                        self.updateMembers();
                        break;
                }
            }
        };

        if (!this.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).getBoolean("Initialized", false)) {
            // for now, immediately start initialization screen
            final Intent initIntent = new Intent(
                    MainActivity.this,
                    InitializationActivity.class
            );

            MainActivity.this.startActivityForResult(initIntent, 0);
        }

        else {
            updateMembers();
        }

        inflater =  getLayoutInflater();
        container = findViewById(R.id.member_insert);

        setRoom("S101/A1");
        setGlobalStatus(GlobalStatus.EXTENDED_ACCESS);
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

    public void setGlobalStatus(MainActivity.GlobalStatus stat){
        TextView global = findViewById(R.id.global_status);
        switch (stat){
            case COME_IN:{
                global.setText(getString(R.string.come_in));
                global.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            }
            case DO_NOT_DISTURB:{
                global.setText(getString(R.string.do_not_disturb));
                global.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            }
            case EXTENDED_ACCESS:{
                global.setText(getString(R.string.extended_access));
                global.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                break;
            }
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
