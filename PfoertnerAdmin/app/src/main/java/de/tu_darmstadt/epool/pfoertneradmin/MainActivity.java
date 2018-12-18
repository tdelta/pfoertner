package de.tu_darmstadt.epool.pfoertneradmin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertneradmin.PfoertnerService.LoginCredentials;
import de.tu_darmstadt.epool.pfoertneradmin.PfoertnerService.PfoertnerService;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;
    private PfoertnerService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create retrofit client




        String API_BASE_URL = "http://deh.duckdns.com/api/";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit = builder.client(httpClient.build()).build();

       service =  retrofit.create(PfoertnerService.class);

        //////

        settings = getSharedPreferences("Settings", 0);

        // "Proof of concept" for persistence variable in memory
        if (settings.getString("token",null ) == null){

            Intent intent = new Intent(this, InitActivity.class);
//            intent.putExtra("PfoertnerService", service);
            startActivity(intent);


        } else{
            AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
            something.setMessage("Die App wurde bereits mit einem Token initialisiert. Willkommen im Startbereich.");
            something.show();
        }

        AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
        something.setMessage("Hier wird wieder eingestiegen.");
        something.show();
    }

//    @SuppressLint("StaticFieldLeak")
//    private void testApi() {
//        // Debug logging
//        //HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        //    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        //    OkHttpClient client = new OkHttpClient.Builder()
//        //                    .addInterceptor(interceptor).build();
//
//        final Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://172.18.84.214:3000")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        final PfoertnerService service = retrofit.create(PfoertnerService.class);
//
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground( final Void ... params ) {
//                try {
//                    final Response response = service.createUser(new LoginCredentials("lol@lol.de", "lol")).execute();
//
//                    Log.d("MainActivity", response.message());
//                }
//
//                catch (final IOException e) {
//                    Log.d("MainActivity", "trolololo");
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute( final Void result ) {
//            }
//        }.execute();
//    }

}
