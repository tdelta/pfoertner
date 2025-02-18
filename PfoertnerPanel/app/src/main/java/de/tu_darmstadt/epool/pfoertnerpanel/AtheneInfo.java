package de.tu_darmstadt.epool.pfoertnerpanel;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that displays usabiility information for the door panel
 * Athene card support feature
 */
public class AtheneInfo extends AppCompatActivity {
    private static final String TAG = "AtheneInfo";


    /**
     * Sets the contentview, initializes the statusbar
     * @param savedInstanceState saved values, unused
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athene_info);

        final TextView room = findViewById(R.id.schedule_room);

        PanelApplication app = PanelApplication.get(this);
        app.getRepo()
                .getOfficeRepo()
                .getOffice(app.getOffice().getId())
                .observe(this,
                        office -> {
                            if (office.getRoom() == null) {
                                room.setText("Room Name Not Set");
                            } else {
                                room.setText(office.getRoom());
                            }
                        });

        int memberId = getIntent().getIntExtra("MemberId", -1);

        if (memberId < 0) {
            Log.e(TAG, "A member must be selected to show appointments.");
        } else {
            app
                    .getRepo()
                    .getMemberRepo()
                    .getMember(memberId)
                    .observe(this, member -> {
                        final TextView appointmentMemberName = (TextView) findViewById(R.id.appointment_member);
                        appointmentMemberName.setText("Appointment times for: " + member.getFirstName() + " " + member.getLastName());
                    });
        }
    }

    /**
     * onClick method to close the activity
     * @param view the view that invoked this method
     */
    public void closeActivity(View view) {
        finish();
    }
}
