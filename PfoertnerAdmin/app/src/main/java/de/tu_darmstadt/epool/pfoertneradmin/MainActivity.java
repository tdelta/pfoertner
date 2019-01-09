package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;
    private PfoertnerService service;
    private State state = State.getInstance();

    private Authentication authtoken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create retrofit client
        service =  State.getInstance().service;


        settings = getSharedPreferences("Settings", 0);

        // "Proof of concept" for persistence variable in memory
        if (settings.getString("token",null ) == null){

            Intent intent = new Intent(this, InitActivity.class);
            startActivity(intent);


        } else{
            AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
            something.setMessage("Die App wurde bereits mit einem Token initialisiert. Willkommen im Startbereich.");
            something.show();
        }
    }

    private void showQRCode() {

        final Context self = this;

        new RequestTask<Office>(){

            Office office;

            @Override
            protected Office doRequests(){
                return Office.loadOffice(settings,service,authtoken);
            }

            @Override
            protected void onSuccess(final Office office){

                final ImageView qrCodeView = findViewById(R.id.qrCodeView);
                final QRCode qrCode = new QRCode(
                        new QRCodeData(office).serialize()
                );

                qrCodeView.setImageDrawable(qrCode);
            }

            @Override
            protected void onException(Exception e){
                ErrorInfoDialog.show(self, e.getMessage(), aVoid -> showQRCode());
            }

        }.execute();

    }
    
}
