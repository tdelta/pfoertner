package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.LoginCredentials;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;

public class InitActivity extends AppCompatActivity {

    private SharedPreferences settings;
    private PfoertnerService service;
    private State state = State.getInstance();
    private String password;
    private int userid;
    private Authentication authtoken;

    private void initApp(){
        final Context self = this;

        new RequestTask<Void>() {
            @Override
            protected Void doRequests(){
                Log.d("DEBUG", "IN initAPP");
                final Password password = Password.loadPassword(settings);

                // First api call

                final User device = User.loadDevice(settings, service, password);
                Log.d("RESULT", "ID: " +device.id);



                // Create logincredentials with the generated password and the id from the server
                final LoginCredentials logincredentials = new LoginCredentials(password.password, device.id);

                // Second api call
                final Authentication authtoken = Authentication.authenticate(settings, service, device, password, self);
                Log.d("RESULT", "ID: " +authtoken.id);
                Log.d("RESULT", "UserID: " +authtoken.userId);

                // Update the static State, because we want to access the Authentoken in
                // MainActivity
                State.getInstance().authtoken = authtoken;

                return null;
            }

            @Override
            protected void onException(Exception e){
                ErrorInfoDialog.show(self, e.getMessage(), aVoid -> initApp());
            }
        }.execute();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        //get persistent memory
        settings = getSharedPreferences("Settings", 0);

        //get retrofit client from State
        service = state.service;

        Log.d("DEBUG", "VOR initAPP");
        initApp();
    }

    public void scanQR(View view){

        IntentIntegrator scanner = new IntentIntegrator(this);
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
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
