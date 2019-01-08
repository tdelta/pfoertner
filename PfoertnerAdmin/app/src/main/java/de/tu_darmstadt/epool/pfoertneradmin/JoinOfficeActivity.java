package de.tu_darmstadt.epool.pfoertneradmin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.tu_darmstadt.epool.pfoertner.common.QRCodeData;
import de.tu_darmstadt.epool.pfoertneradmin.R;

public class JoinOfficeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init2);

        QRCodeData qrData = QRCodeData.deserialize(getIntent().getStringExtra("QrCodeDataRaw"));

    }

    public void createAccount(View view){
        final EditText firstnameinput = (EditText) findViewById(R.id.VornameInput);
        final String firstname = firstnameinput.getText().toString();
        Log.e("ERROR", firstname);

        final EditText lastnameinput = (EditText) findViewById(R.id.NachnameInput);
        final String lastname = lastnameinput.getText().toString();

        Log.e("ERROR", lastname);

        finish();
    }
}
