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

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.retrofit.LoginCredentials;
import de.tu_darmstadt.epool.pfoertner.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.retrofit.User;
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

        // Source : https://stackoverflow.com/a/8833123
        try {

            // Try to use the QR scanner from zxing
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            startActivityForResult(intent, 0);

        } catch (Exception e) {

            // If the QR scanner from zxing is not installed, the application redirects to the
            // google play store to download the QR scanner.

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);

        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handel results from QR code scanner
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
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
            if(resultCode == RESULT_CANCELED){
                AlertDialog something = new AlertDialog.Builder(InitActivity.this).create();
                something.setMessage("Scanvorgang wurde abgebrochen!");
                something.show();
            }
        }
    }
}
