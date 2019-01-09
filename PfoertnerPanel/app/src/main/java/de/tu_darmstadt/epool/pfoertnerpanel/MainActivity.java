package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
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

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class MainActivity extends AppCompatActivity {
    private LayoutInflater inflater;
    private ViewGroup container;
    private TableRow row;
    private int memberCount = 0;

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

        if (!this.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).getBoolean("Initialized", false)) {
            // for now, immediately start initialization screen
            final Intent initIntent = new Intent(
                    MainActivity.this,
                    InitializationActivity.class
            );

            MainActivity.this.startActivity(initIntent);
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

    public void addMember(View view){

        // set the attributes of the member to add
        String[] work = {"Mo-Fr 8:00 - 23:00", "Sa-So 8:00 - 23:00"};
        Member member = new Member(this);
        member.setName("Prof. Dr. Ing. Max Mustermann");
        member.setStatus(Member.Status.OUT_OF_OFFICE);
        member.setOfficeHours(work);
        member.setImage(getDrawable(R.drawable.ic_contact_default));

        switch(memberCount){
            case 0:{
                row = (TableRow) inflater.inflate(R.layout.table_row, container, false);
                inflater.inflate(R.layout.space, row);
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                container.addView(row);
                memberCount++;
                break;
            }
            case 1:{
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                memberCount++;
                break;
            }
            case 2:{
                row = (TableRow) inflater.inflate(R.layout.table_row, container, false);
                inflater.inflate(R.layout.space, row);
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                container.addView(row);
                memberCount++;
                break;
            }
            case 3:{
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                memberCount++;
                break;
            }
        }
    }


}
