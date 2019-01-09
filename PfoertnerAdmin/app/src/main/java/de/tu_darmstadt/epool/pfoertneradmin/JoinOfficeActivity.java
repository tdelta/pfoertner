package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeJoinCode;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Person;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PersonCreationData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.tasks.JoinOfficeTask;
import retrofit2.Retrofit;

public class JoinOfficeActivity extends AppCompatActivity {

    private State state = State.getInstance();
    private SharedPreferences settings;
    private PfoertnerService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init2);

        settings = getSharedPreferences("Settings", 0);
        service = state.service;
    }

    private void joinOffice(String lastName, String firstName, Office office) {
        Context self = this;
        Authentication authtoken = state.authtoken;

        new RequestTask<Void>() {


            @Override
            protected Void doRequests() {

                // Create Person
                Person.loadPerson(new PersonCreationData(lastName,firstName), settings, service, authtoken);
                // Join Office
                Office.joinOffice(service,authtoken, office);

                return null;
            }

            @Override
            protected void onSuccess(final Void v) {
                ((JoinOfficeActivity) self).finish();
            }

            @Override
            protected void onException(Exception e) {
                ErrorInfoDialog.show(self, e.getMessage(), aVoid -> joinOffice(lastName,firstName,office));
            }
        }.execute();
    }


    public void createAccount(View view){
        final EditText firstnameinput = (EditText) findViewById(R.id.VornameInput);
        final String firstName = firstnameinput.getText().toString();
        Log.e("ERROR", firstName);

        final EditText lastnameinput = (EditText) findViewById(R.id.NachnameInput);
        final String lastName = lastnameinput.getText().toString();

        Log.e("ERROR", lastName);

        QRCodeData qrData = QRCodeData.deserialize(getIntent().getStringExtra("QrCodeDataRaw"));

        Log.e("ERROR", "blabla");

        joinOffice(lastName, firstName, new Office(qrData.officeId, qrData.joinCode));

        // TODO: Remaining to test, server functionality isnt implemented yet

        /*
        new JoinOfficeTask(state.service,
                settings, state.authtoken,
                new Office(qrData.officeId,
                        qrData.joinCode),
                firstName,
                lastName).execute();
        */

        //finish();
    }
}
