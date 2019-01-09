package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;


import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;

import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class InitializationActivity extends AppCompatActivity {
    private static final String TAG = "InitializationActivity";

    private RequestTask<Office> initTask = new RequestTask<>();

    private EventChannel eventChannel;

    @Override
    protected void onStart() {
        super.onStart();
        eventChannel.listen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventChannel.shutdown();
    }

    private void initPanel(final Context context, final Consumer<Void> closeSplashScreen) {
        final PfoertnerService service = PfoertnerService.makeService();
        final SharedPreferences registrationInfo = context.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);

        this.initTask = new RequestTask<Office>() {
            @Override
            protected Office doRequests() {
                final Password password = Password.loadPassword(registrationInfo);
                final User device = User.loadDevice(registrationInfo, service, password);
                final Authentication authToken = Authentication.authenticate(registrationInfo, service, device, password, context);
                final Office office = Office.createOffice(registrationInfo, service, authToken);

                return office;
            }

            @Override
            protected void onSuccess(Office result) {
                showQRCode(result);

                closeSplashScreen.accept(null);
            }

            @Override
            protected void onException(Exception e) {
                ErrorInfoDialog.show(context, e.getMessage(), aVoid -> initPanel(context, closeSplashScreen));
            }
        };

        this.initTask.execute();
    }

    private void showQRCode(final Office office) {
        final String displayedData = new QRCodeData(office).serialize();
        Log.d(TAG, "Displaying QRData: " + displayedData);

        final ImageView qrCodeView = findViewById(R.id.qrCodeView);
        final QRCode qrCode = new QRCode(
                displayedData
        );

        qrCodeView.setImageDrawable(qrCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialization);

        final InitializationActivity self = this;
        eventChannel = new EventChannel(this) {
            @Override
            protected void onEvent(EventType eventType) {
                switch (eventType) {
                    case AdminJoined:
                        initTask.whenDone(aVoid -> {
                            // Close initialization, as soon as a member has been registered

                            // Remember, that the app has been initialized:
                            final SharedPreferences.Editor e = self.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE).edit();

                            e.putBoolean("Initialized", true);
                            e.apply();

                            self.finish();
                        });
                        break;
                }
            }
        };

        SplashScreenActivity.run(this,
            this::initPanel
        );
    }

    @Override
    public void onBackPressed() {
        // prevent the user from removing the initialization activity from the activity stack. (pressing back button)
        // instead, move the entire app into the background
        moveTaskToBack(true);
    }
}
