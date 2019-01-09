package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.spencerwi.either.Either;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.LoginCredentials;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;

import de.tu_darmstadt.epool.pfoertneradmin.tasks.InitTask;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InitActivity extends AppCompatActivity {

    private SharedPreferences settings;
    private PfoertnerService service;
    private State state = State.getInstance();
    private String password;
    private int userid;
    private Authentication authtoken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        //get persistent memory
        settings = getSharedPreferences("Settings", 0);

        //get retrofit client from State
        service = state.service;

        new InitTask(
                service,
                settings,
                eitherErrorOrVoid -> {
                    if (eitherErrorOrVoid.isLeft()) {
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setMessage(
                                "We encountered a problem:\n\n" +
                                        eitherErrorOrVoid.getLeft()
                        );
                        alertDialogBuilder.show();
                    }
                }
        ).execute();


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
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
