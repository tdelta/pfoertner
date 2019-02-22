package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;


import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;

import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.OfficeObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import io.reactivex.disposables.CompositeDisposable;

public class InitializationActivity extends AppCompatActivity {
    private static final String TAG = "InitializationActivity";

    private RequestTask<Office> initTask;

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
                // TODO: Proper disposing of subscription according to lifecycle
                app
                    .setOffice(office)
                    .subscribe(
                            () -> {
                                showQRCode(app.getOffice());

                                office.addObserver(
                                        new OfficeObserver() {
                                            @Override
                                            public void onJoinCodeChanged(String newJoinCode) {
                                                InitializationActivity.this.showQRCode(office);
                                            }
                                        }
                                );

                                closeSplashScreen.accept(null);
                            },
                            throwable -> {
                                Log.e(TAG, "Failed to create an office. Asking the user to retry...", throwable);

                                ErrorInfoDialog.show(context, throwable.getMessage(), aVoid -> initPanel(context, closeSplashScreen));
                            }
                    );
            }

            @Override
            protected void onException(final Exception e) {
                ErrorInfoDialog.show(context, e.getMessage(), aVoid -> initPanel(context, closeSplashScreen));
            }
        };

        this.initTask.whenDone(
                aVoid -> this.initTask.execute()
        );
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
