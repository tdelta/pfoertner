package de.tu_darmstadt.epool.pfoertneradmin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import io.reactivex.disposables.CompositeDisposable;

public class InitActivity extends AppCompatActivity {
    private static final String TAG = "InitActivity";
    private CompositeDisposable disposables;

    private void initApp(){
        final PfoertnerApplication app = PfoertnerApplication.get(InitActivity.this);

        disposables.add(
                app
                    .init()
                    .subscribe(
                            () -> InitActivity.this.setContentView(R.layout.activity_init),
                            throwable -> {
                                Log.e(TAG, "Could not initialize app.", throwable);

                                ErrorInfoDialog.show(InitActivity.this, throwable.getMessage(), aVoid -> initApp());
                            }
                    )
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (disposables != null) {
            disposables.dispose();
        }
        disposables = new CompositeDisposable();

        initApp();
    }

    @Override
    protected void onStop() {
        super.onStop();

        disposables.dispose();
    }

    public void scanQR(View view){
        IntentIntegrator scanner = new IntentIntegrator(this);

        scanner.setCaptureActivity(CaptureActivity.class);
        scanner.setOrientationLocked(false);
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
                Log.d("DEBUG","GOT SCAN DATA");
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
