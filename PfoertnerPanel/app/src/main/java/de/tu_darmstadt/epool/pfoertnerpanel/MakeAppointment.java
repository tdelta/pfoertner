package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MakeAppointment extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_appointment);
        TextView title = findViewById(R.id.textView3);
        TextView underTitle = findViewById(R.id.textView5);
        title.setText("One last step required");
        underTitle.setText("Please enter your name, mail and a message. You can also choose your time within the displayed range.");
    }
}
