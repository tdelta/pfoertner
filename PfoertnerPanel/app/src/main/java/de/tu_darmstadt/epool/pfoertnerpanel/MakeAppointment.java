package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MakeAppointment extends AppCompatActivity {

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
}
