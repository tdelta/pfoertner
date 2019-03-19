package de.tu_darmstadt.epool.pfoertnerpanel;

import android.app.Activity;
import android.content.Intent;
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
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.AppointmentEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.helpers.AtheneReader;
import io.reactivex.schedulers.Schedulers;

public class MakeAppointment extends AppCompatActivity {
    private static final String TAG = "MakeAppointment";
    private final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private AtheneReader atheneReader;

    private String atheneId;

    /**
     * Is called when activity gets created
     * Initializes User Interface
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        atheneReader = new AtheneReader(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_make_appointment);

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

    @Override
    public void onNewIntent(Intent intent){
        if(atheneReader.isTechDiscovered(intent)){
            Log.d(TAG,"Received tech discovered intent");
            atheneReader.beep();
            atheneId = atheneReader.extractAtheneId(intent);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        atheneReader.resume();
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

        final int officeMemberId = getIntent().getIntExtra("MemberId", -1);

        final AppointmentEntity request = new AppointmentEntity(
                0,
                start,
                end,
                email.getText().toString(),
                name.getText().toString(),
                message.getText().toString(),
                false,
                officeMemberId,
                atheneId
        );


        PfoertnerApplication app = PfoertnerApplication.get(this);

        app
                .getRepo()
                .getAppointmentRepository()
                .createAppointment(request)
                .observeOn(Schedulers.io())
        .subscribe(
                () -> Toast.makeText(this, "Request has been sent!", Toast.LENGTH_LONG).show(),
                throwable -> {
                    Toast.makeText(this, "Error sending appointment request", Toast.LENGTH_LONG).show();
                    Log.e(TAG,"Error sending appointment request",throwable);
                }
        );

        finish();
    }

    public void onAtheneInfo(View view) {
        final Intent intent = new Intent(this, AtheneInfo.class);
        intent.putExtra("MemberId", getIntent().getIntExtra("MemberId",-1));
        startActivity(intent);
    }
}
