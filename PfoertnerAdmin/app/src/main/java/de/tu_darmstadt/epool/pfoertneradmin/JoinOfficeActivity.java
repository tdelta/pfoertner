package de.tu_darmstadt.epool.pfoertneradmin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Person;

public class JoinOfficeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init2);
    }

    private void joinOffice(String lastName, String firstName, final int officeId, final String joinCode) {
        new RequestTask<Void>() {
            @Override
            protected Void doRequests() {
                final PfoertnerApplication app = PfoertnerApplication.get(JoinOfficeActivity.this);

                final Person p = Office.joinOffice(
                        officeId,
                        joinCode,
                        firstName,
                        lastName,
                        app.getSettings(),
                        app.getService(),
                        app.getAuthentication()
                );

                final Office office = Office.loadOffice(
                        officeId,
                        app.getSettings(),
                        app.getService(),
                        app.getAuthentication()
                );

                app.setOffice(office);

                return null;
            }

            @Override
            protected void onSuccess(final Void v) {
                JoinOfficeActivity.this.finish();
            }

            @Override
            protected void onException(Exception e) {
                ErrorInfoDialog.show(JoinOfficeActivity.this, e.getMessage(), aVoid -> joinOffice(lastName,firstName,officeId,joinCode));
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

        Log.e("ERROR", "Read join code: " + qrData.joinCode);

        joinOffice(lastName, firstName, qrData.officeId, qrData.joinCode);
    }
}
