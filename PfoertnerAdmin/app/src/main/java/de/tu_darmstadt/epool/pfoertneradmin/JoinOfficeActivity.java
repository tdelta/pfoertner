package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.activities.SplashScreenActivity;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;

public class JoinOfficeActivity extends AppCompatActivity {
    private static final String TAG = "JoinOfficeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init2);
    }

    /**
     * This method creates a new officemember with network requests
     * to the server. If the creation is successful, then the updated
     * office is inserted into the application repository. If there is
     * an error during the creation process, there will be an error window
     * displaying the error.
     *
     * @param splashScreenActivity
     * @param closeSplashScreen
     * @param lastName of the newly added officemember
     * @param firstName of the newly added officemember
     * @param status of the newly added officemember
     * @param officeId of the office, where the new officemember was added
     * @param joinCode of the office, where the new officemembe was added
     */
    private void joinOffice(final SplashScreenActivity splashScreenActivity, final Consumer<Void> closeSplashScreen, String lastName, String firstName, String status, final int officeId, final String joinCode) {
        final AdminApplication app = AdminApplication.get(JoinOfficeActivity.this);

        new RequestTask<Pair<Office, MemberData>>() {
            @Override
            protected Pair<Office, MemberData> doRequests() {
                final MemberData m = Member.joinOffice(
                        officeId,
                        joinCode,
                        firstName,
                        lastName,
                        status,
                        app.getSettings(),
                        app.getService(),
                        app.getAuthentication()
                );

                final Office office = Office.loadOffice(
                        officeId,
                        app.getSettings(),
                        app.getService(),
                        app.getAuthentication(),
                        app.getFilesDir()
                );

                return new Pair<>(office, m);
            }

            @Override
            @SuppressWarnings("CheckResult")
            protected void onSuccess(final Pair<Office, MemberData> result) {
                app
                        .setOffice(result.first)
                        .subscribe(
                                () -> {
                                    app.setMemberId(result.second.id);

                                    // Leave activity, as soon as member data is available
                                    app
                                            .getRepo()
                                            .getMemberRepo()
                                            .getMember(app.getMemberId())
                                            .observe(splashScreenActivity, member -> {
                                                if (member != null) {
                                                    JoinOfficeActivity.this.finish();
                                                    closeSplashScreen.accept(null);
                                                }
                                            });
                                },
                                throwable -> Log.e(TAG, "Failed to register office after joining.", throwable)
                        );
            }

            @Override
            protected void onException(Exception e) {
                Log.e(TAG, "Failed to join office", e);

                ErrorInfoDialog.show(splashScreenActivity, e.getMessage(), aVoid -> joinOffice(splashScreenActivity, closeSplashScreen, lastName,firstName,"Available",officeId,joinCode),false);
            }
        }.execute();
    }

    /**
     * This method is called when pressing the createAccount button during
     * the initialization of the app. Then this method takes the input from
     * the firstName and lastName textInputs and creates via a network call
     * (joinOffice()) a new officemember account
     *
     * @param view of the button createAccount
     */
    public void createAccount(View view){
        final EditText firstnameinput = (EditText) findViewById(R.id.VornameInput);
        final String firstName = firstnameinput.getText().toString();
        Log.e(TAG, "Got first name: " + firstName);

        final EditText lastnameinput = (EditText) findViewById(R.id.NachnameInput);
        final String lastName = lastnameinput.getText().toString();

        Log.e(TAG, "Got last name: " + lastName);

        QRCodeData qrData = QRCodeData.deserialize(getIntent().getStringExtra("QrCodeDataRaw"));

        Log.e(TAG, "Read join code: " + qrData.joinCode);

        SplashScreenActivity.run(
                this,
                LayoutInflater.from(this).inflate(R.layout.activity_splash_screen, null),
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                (splashScreenActivity, closeSplashScreen) ->
                        joinOffice(splashScreenActivity, closeSplashScreen, lastName, firstName, "", qrData.officeId, qrData.joinCode)
        );
    }
}
