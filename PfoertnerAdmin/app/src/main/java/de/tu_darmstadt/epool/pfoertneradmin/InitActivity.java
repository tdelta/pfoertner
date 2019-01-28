package de.tu_darmstadt.epool.pfoertneradmin;

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

public class InitActivity extends AppCompatActivity {
    private void initApp(){
        new RequestTask<Void>() {
            @Override
            protected Void doRequests(){
                final PfoertnerApplication app = PfoertnerApplication.get(InitActivity.this);

                app.init();

                return null;
            }

            @Override
            protected void onException(Exception e){
                ErrorInfoDialog.show(InitActivity.this, e.getMessage(), aVoid -> initApp());
            }
        }.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        initApp();
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
}
