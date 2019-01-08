package de.tu_darmstadt.epool.pfoertneradmin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.tu_darmstadt.epool.pfoertneradmin.R;

public class JoinOfficeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init2);
    }

    public void createAccount(View view){

        EditText firstnameinput = (EditText) findViewById(R.id.VornameInput);
        String firstname = firstnameinput.getText().toString();
        Log.e("ERROR", firstname);

        EditText lastnameinput = (EditText) findViewById(R.id.NachnameInput);
        String lastname = lastnameinput.getText().toString();

        Log.e("ERROR", lastname);

        finish();
    }
}
