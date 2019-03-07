package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;


import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.activities.SplashScreenActivity;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;

import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import io.reactivex.disposables.CompositeDisposable;

public class InitializationActivity extends AppCompatActivity {
    private static final String TAG = "InitializationActivityLog";

    private RequestTask<Office> initTask;
    private EventChannel eventChannel;

    private CompositeDisposable disposables;

    @Override
    protected void onStart() {
        super.onStart();
        eventChannel.listen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventChannel.shutdown();
        disposables.dispose();
    }

    private void initPanel(final SplashScreenActivity splashScreenActivity, final Consumer<Void> closeSplashScreen) {
        final PfoertnerApplication app = PfoertnerApplication.get(InitializationActivity.this);

        this.initTask = new RequestTask<Office>() {
            @Override
            protected Office doRequests() {
                final Office office = Office.createOffice(
                        app.getSettings(),
                        app.getService(),
                        app.getAuthentication(),
                        app.getFilesDir()
                );

                return office;
            }

            @Override
            @SuppressWarnings("CheckResult")
            protected void onSuccess(final Office office) {
                app
                    .setOffice(office)
                    .subscribe(
                            () -> {
                                Log.d(TAG, "The office has been set. We will now wait, until the necessary data is available, to display the QR code.");

                                app
                                        .getRepo()
                                        .getOfficeRepo()
                                        .getOffice(office.getId())
                                        .observe(InitializationActivity.this, changedOffice -> {
                                            if (changedOffice != null) {
                                                Log.d(TAG, "Displaying QR code...");

                                                InitializationActivity.this.showQRCode(changedOffice);
                                            }

                                            else {
                                                Log.d(TAG, "Office data to display the QR code is not available yet, waiting till it is...");
                                            }
                                        });

                                app
                                        .getRepo()
                                        .getDeviceRepo()
                                        .getDevice(app.getDevice().id)
                                        .observe(splashScreenActivity, device -> {
                                            // wait until there is a fcm token
                                            if (device != null) {
                                                if (device.getFcmToken() != null) {
                                                    closeSplashScreen.accept(null);
                                                }

                                                else {
                                                    Log.d(TAG, "Can not yet remove initialization screen, since there is no fcm token set yet.");
                                                }
                                            }

                                            else {
                                                Log.d(TAG, "Can not yet remove initialization screen, since there is no device set yet.");
                                            }
                                        });
                            },
                            throwable -> {
                                Log.e(TAG, "Failed to create an office. Asking the user to retry...", throwable);

                                ErrorInfoDialog.show(splashScreenActivity, throwable.getMessage(), aVoid -> initPanel(splashScreenActivity, closeSplashScreen), false);
                            }
                    );
            }

            @Override
            protected void onException(final Exception e) {
                ErrorInfoDialog.show(splashScreenActivity, e.getMessage(), aVoid -> initPanel(splashScreenActivity, closeSplashScreen), false);
            }
        };

        this.initTask.whenDone(
                aVoid -> this.initTask.execute()
        );
    }

    private void showQRCode(final de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office office) {
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

        if (this.disposables != null) {
            this.disposables.dispose();
        }

        this.disposables = new CompositeDisposable();

        eventChannel = new EventChannel(this) {
            @Override
            protected void onEvent(final EventType eventType, final @Nullable String payload) {
                switch (eventType) {
                    case AdminJoined:
                        initTask.whenDone(aVoid -> {
                            // Close initialization, as soon as a member has been registered

                            // Remember, that the app has been initialized:
                            final PfoertnerApplication app = PfoertnerApplication.get(InitializationActivity.this);

                            final SharedPreferences.Editor e = app.getSettings().edit();

                            e.putBoolean("Initialized", true);
                            e.apply();

                            InitializationActivity.this.setResult(RESULT_OK, new Intent());
                            InitializationActivity.this.finish();
                        });
                        break;
                }
            }
        };

        SplashScreenActivity.run(this,
            LayoutInflater.from(this).inflate(R.layout.activity_splash_screen, null),
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE,
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
