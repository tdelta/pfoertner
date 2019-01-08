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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.io.IOException;

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

        // start Asynchrone thread, which makes the init calls
        new InitTask(service, settings).execute();
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
                String token = data.getStringExtra("SCAN_RESULT");
                AlertDialog something = new AlertDialog.Builder(InitActivity.this).create();
                something.setMessage(token);
                something.show();

                // Save the token in persistent memory
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("token", token);
                editor.commit();

                // Set new intent for entering user information

                Intent joinOffice = new Intent(this, JoinOfficeActivity.class);
                startActivity(joinOffice);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
