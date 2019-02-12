package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;

public class MakeAppointment extends AppCompatActivity {
    String TAG = "MakeAppointment";
    private SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_appointment);
        TextView title = findViewById(R.id.textView3);
        title.setText("One last step required");
        TextView underTitle = findViewById(R.id.textView5);
        underTitle.setText("Please enter your name, mail and a message.");
        Button confirm = findViewById(R.id.confirmbutton);
        confirm.setText("CONFIRM APPOINTMENT");
        TextInputLayout enterName = findViewById(R.id.textInputLayout);
        enterName.setHint("Enter your full name here");
        TextInputLayout enterEmail = findViewById(R.id.textInputLayout6);
        enterEmail.setHint("Enter your email address here");
        TextInputLayout enterMessage = findViewById(R.id.textInputLayout7);
        enterMessage.setHint("Enter a message");
        TextView appointmentTime = findViewById(R.id.textView6);
        appointmentTime.setText(getIntent().getStringExtra("appointmentTime"));
    }

    public void onConfirmAppointment(View view){
        // yyyy-MM-dd HH:mm -> itentstrings -> parse to LocalDateTime -> richtige String darstellung
        // (musste so weil sonst der monat nicht richtig was 2 statt 02)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.of(getIntent().getIntExtra("Year", 2019)
                , getIntent().getIntExtra("Month", 1)
                , getIntent().getIntExtra("Day", 1)
                , Integer.valueOf(getIntent().getStringExtra("appointmentTime").substring(0,2))
                , Integer.valueOf(getIntent().getStringExtra("appointmentTime").substring(3,5)));
        String startTime = dateTime.format(formatter);

        dateTime = LocalDateTime.of(getIntent().getIntExtra("Year", 2019)
                , getIntent().getIntExtra("Month", 1)
                , getIntent().getIntExtra("Day", 1)
                , Integer.valueOf(getIntent().getStringExtra("appointmentTime").substring(8,10))
                , Integer.valueOf(getIntent().getStringExtra("appointmentTime").substring(11,13)));
        String endTime = dateTime.format(formatter);

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
        TextView email = (TextView) findViewById(R.id.emailInput);
        TextView name = (TextView) findViewById(R.id.nameInput);
        TextView message = (TextView) findViewById(R.id.messageInput);

        AppointmentRequest request = new AppointmentRequest(start, end, email.getText().toString(), name.getText().toString(), message.getText().toString(), false);


        new RequestTask<Void>(){
            @Override
            protected Void doRequests() throws Exception {
                PfoertnerApplication app = PfoertnerApplication.get(MakeAppointment.this);
                app.getService().createNewAppointment(getIntent().getIntExtra("MemberId", 0), request);
                return null;
            }

            @Override
            protected void onException(Exception e) {
                super.onException(e);
            }
        }.execute();
    }
}
