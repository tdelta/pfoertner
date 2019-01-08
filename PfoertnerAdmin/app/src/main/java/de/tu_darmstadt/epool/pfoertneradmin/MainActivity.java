package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import de.tu_darmstadt.epool.pfoertner.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.retrofit.PfoertnerService;
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
}
