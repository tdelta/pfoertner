package de.tu_darmstadt.epool.pfoertnerpanel;

import android.app.Activity;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class MakeAppointment extends AppCompatActivity {
    private static final String TAG = "MakeAppointment";
    private final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Is called when activity gets created
     * Initializes User Interface
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_make_appointment);

        final TextView title = findViewById(R.id.textView3);
        title.setText("One last step required");

        final TextView underTitle = findViewById(R.id.textView5);
        underTitle.setText("Please enter your name, mail and a message.");

        final Button confirm = findViewById(R.id.confirmbutton);
        confirm.setText("REQUEST APPOINTMENT");

        final TextInputLayout enterName = findViewById(R.id.textInputLayout);
        enterName.setHint("Enter your full name here");

        final TextInputLayout enterEmail = findViewById(R.id.textInputLayout6);
        enterEmail.setHint("Enter your email address here");

        final TextInputLayout enterMessage = findViewById(R.id.textInputLayout7);
        enterMessage.setHint("Enter a message");

        final TextView appointmentTime = findViewById(R.id.textView6);
        appointmentTime.setText(getIntent().getIntExtra("appointmentStartTimeHour", -1)
                +":"
                +getIntent().getStringExtra("appointmentStartTimeMinutes")
                +" - "
                +getIntent().getIntExtra("appointmentEndTimeHour", -1)
                +":"
                +getIntent().getStringExtra("appointmentEndTimeMinutes")
        );
    }

    /**
     * Executed when button confirm is pressed.
     * Gets infos about the chosen event from the intent, create a new Date object,
     * sends request with the selected Date to the server
     * @param view view context of layout
     */
    public void onConfirmAppointment(View view){
        // yyyy-MM-dd HH:mm -> itentstrings -> parse to LocalDateTime -> richtige String darstellung
        // (musste so weil sonst der monat nicht richtig was 2 statt 02)
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.of(getIntent().getIntExtra("Year", 2019)
                , getIntent().getIntExtra("Month", 1)
                , getIntent().getIntExtra("Day", 1)
                , getIntent().getIntExtra("appointmentStartTimeHour", -1)
                , Integer.valueOf(getIntent().getStringExtra("appointmentStartTimeMinutes")));

        final String startTime = dateTime.format(formatter);

        dateTime = LocalDateTime.of(getIntent().getIntExtra("Year", 2019)
                , getIntent().getIntExtra("Month", 1)
                , getIntent().getIntExtra("Day", 1)
                , getIntent().getIntExtra("appointmentEndTimeHour", -1)
                , Integer.valueOf(getIntent().getStringExtra("appointmentEndTimeMinutes")));
        final String endTime = dateTime.format(formatter);

//        Log.d(TAG, startTime);
//        Log.d(TAG, endTime);
// Martin wills doch nicht als string also wird aus dem string wieder ein date geparst...w

        Date start = null;
        Date end = null;
        try {
            start = dateParser.parse(startTime);
            end = dateParser.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "start for request" + start.toString());
        Log.d(TAG, "end for request" + end.toString());

        //Build request
        final TextView email = (TextView) findViewById(R.id.emailInput);
        final TextView name = (TextView) findViewById(R.id.nameInput);
        final TextView message = (TextView) findViewById(R.id.messageInput);

        final AppointmentRequest request = new AppointmentRequest(
                start,
                end,
                email.getText().toString(),
                name.getText().toString(),
                message.getText().toString(),
                false
        );


        new RequestTask<Void>(){
            @Override
            protected Void doRequests() throws Exception {
                final PfoertnerApplication app = PfoertnerApplication.get(MakeAppointment.this);

                app
                        .getService()
                        .createNewAppointment(app.getAuthentication().id, getIntent().getIntExtra("MemberId", 0), request).execute();

                return null;
            }

            @Override
            protected void onException(Exception e) {
                super.onException(e);
            }
        }.execute();

        Toast.makeText(this, "Request has been sent!", Toast.LENGTH_LONG).show();

        finish();
    }
}
