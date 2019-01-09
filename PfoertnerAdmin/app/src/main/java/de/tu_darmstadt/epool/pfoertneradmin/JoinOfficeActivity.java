package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.tu_darmstadt.epool.pfoertner.common.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.tasks.JoinOfficeTask;

public class JoinOfficeActivity extends AppCompatActivity {

    private State state = State.getInstance();
    private SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init2);

        settings = getSharedPreferences("Settings", 0);
    }

    public void createAccount(View view){
        final EditText firstnameinput = (EditText) findViewById(R.id.VornameInput);
        final String firstName = firstnameinput.getText().toString();
        Log.e("ERROR", firstName);

        final EditText lastnameinput = (EditText) findViewById(R.id.NachnameInput);
        final String lastName = lastnameinput.getText().toString();

        Log.e("ERROR", lastName);

        QRCodeData qrData = QRCodeData.deserialize(getIntent().getStringExtra("QrCodeDataRaw"));

        // TODO: Remaining to test, server functionality isnt implemented yet
        /*
        new JoinOfficeTask(state.service,
                settings, state.authtoken,
                new Office(qrData.officeId,
                        qrData.joinCode),
                firstName,
                lastName).execute();
        */

        finish();
    }
}
