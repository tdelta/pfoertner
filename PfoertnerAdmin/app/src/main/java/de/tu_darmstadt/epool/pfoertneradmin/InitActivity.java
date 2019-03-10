package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.activities.SplashScreenActivity;
import io.reactivex.disposables.CompositeDisposable;

public class InitActivity extends AppCompatActivity {
    private static final String TAG = "InitActivityLog";
    private CompositeDisposable disposables;

    private void initApp(final SplashScreenActivity splashScreenActivity, final Consumer<Void> closeSplashScreen){
        final PfoertnerApplication app = PfoertnerApplication.get(InitActivity.this);

        app
            .init()
            .subscribe(
                    () -> {
                        Log.d(TAG, "App has been initialized, will continue, as soon as a fcm token is available.");

                        app
                                .getRepo()
                                .getDeviceRepo()
                                .getDevice(app.getDevice().id)
                                .observe(splashScreenActivity, device -> {
                                    if (device != null) {
                                        if (device.getFcmToken() != null) {
                                            Log.d(TAG, "FCM token is set, closing splash screen...");

                                            InitActivity.this.setContentView(R.layout.activity_init);
                                            closeSplashScreen.accept(null);
                                        }

                                        else {
                                            Log.d(TAG, "Could not close splash screen yet, since there is no fcm token set right now.");
                                        }
                                    }

                                    else {
                                        Log.d(TAG, "Could not close splash screen yet, since there is no device data set right now.");
                                    }
                                });
                    },
                    throwable -> {
                        Log.e(TAG, "Could not initialize app.", throwable);

                        ErrorInfoDialog.show(splashScreenActivity, throwable.getMessage(), aVoid -> initApp(splashScreenActivity, closeSplashScreen),false);
                    }
            );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (disposables != null) {
            disposables.dispose();
        }
        disposables = new CompositeDisposable();

        SplashScreenActivity.run(
                this,
                LayoutInflater.from(this).inflate(R.layout.activity_splash_screen, null),
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                this::initApp
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        disposables.dispose();
    }

    public void scanQR(View view){
        IntentIntegrator scanner = new IntentIntegrator(this);

        scanner.setCaptureActivity(CaptureActivity.class);
        scanner.setOrientationLocked(true);
        scanner.setBeepEnabled(false);
        scanner.setPrompt("Place the panel barcode inside the viewfinder rectangle.");
        scanner.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        // Handel results from QR code scanner
        if(result != null) {
            if(result.getContents() == null) {
                AlertDialog something = new AlertDialog.Builder(InitActivity.this).create();
                something.setMessage("Scanvorgang wurde abgebrochen!");
                something.show();
            } else {
                String qrCodeDataRaw = data.getStringExtra("SCAN_RESULT");

                // Set new intent for entering user information
                Log.d(TAG,"Got qr code scan.");

                Intent joinOffice = new Intent(this, JoinOfficeActivity.class);
                joinOffice.putExtra("QrCodeDataRaw", qrCodeDataRaw );
                startActivity(joinOffice);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed(){
        // prevent the user from removing the splash screen from the activity stack. (pressing back button)
        // instead, move the entire app into the background
        moveTaskToBack(true);
    }
}
