package de.tu_darmstadt.epool.pfoertneradmin;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;

public class JoinOfficeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init2);
    }

    private void joinOffice(String lastName, String firstName, final int officeId, final String joinCode) {
        final AdminApplication app = AdminApplication.get(JoinOfficeActivity.this);

        new RequestTask<Pair<Office, MemberData>>() {
            @Override
            protected Pair<Office, MemberData> doRequests() {
                final MemberData m = Member.joinOffice(
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

                return new Pair<>(office, m);
            }

            @Override
            protected void onSuccess(final Pair<Office, MemberData> result) {
                app.setOffice(result.first);
                app.setMemberId(result.second.id);

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
