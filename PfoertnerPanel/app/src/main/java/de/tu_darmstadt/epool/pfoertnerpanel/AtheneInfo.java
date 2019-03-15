package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AtheneInfo extends AppCompatActivity {
    private static final String TAG = "AtheneInfo";


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

    public void closeActivity(View view) {
        finish();
    }
}
