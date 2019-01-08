package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.LoginCredentials;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InitActivity extends AppCompatActivity {

    private SharedPreferences settings;
    private PfoertnerService service;
    private String password;
    private int userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        settings = getSharedPreferences("Settings", 0);

        //create retrofit client

        String API_BASE_URL = "http://172.18.92.121:3000/api/";

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

        //Create user
        SharedPreferences.Editor edit = settings.edit();
        edit.putString("password", "blabla");
        edit.commit();
        Call<User> call = service.createUser(new Password(settings.getString("password","passwordnotset")));

        Log.d("success", "ich bin vor den ersten call gekommen");

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // The network call was a success and we got a response
                // TODO: use the repository list and display it
                User body = response.body();
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("userid", body.id);
                editor.commit();

                Log.d("success", "ich bin im ersten call in responspe");
                Log.d("bla", "" + body.id);

                Log.d("success", "ich bin nach dem ersten call ");

                Call<Authentication> call2 = service.login(new LoginCredentials(settings.getString("password", "passwordnotset"), settings.getInt("userid", 0)));


                Log.d("success", "Starte call 2");
                call2.enqueue(new Callback<Authentication>() {
                    @Override
                    public void onResponse(Call<Authentication> call, Response<Authentication> response) {
                        // The network call was a success and we got a response
                        // TODO: use the repository list and display it
                        Authentication body = response.body();
                        if(body == null){
                            Log.d("errrror", "geh nix da");
                        }
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("authtoken", body.id);
                        editor.commit();

                        Log.d("success", "ich bin im zweiten call in responspe");
                        Log.d("authtoken", "" + body.id);
                    }

                    @Override
                    public void onFailure(Call<Authentication> call, Throwable t) {
                        // the network call was a failure
                        t.printStackTrace();

                        Log.d("success", "ich bin im zewiten call in failure");
                        Log.d("bla", "versager2");
                    }
                });

                Log.d("success", "ich bin nach dem zweiten call");
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // the network call was a failure
                t.printStackTrace();

                Log.d("success", "ich bin im ersten call in failure");
                Log.d("bla", "versager");
            }
        });



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

    public void createAccount(View view){

        EditText firstnameinput = (EditText) findViewById(R.id.VornameInput);
        String firstname = firstnameinput.getText().toString();
        Log.e("ERROR", firstname);

        EditText lastnameinput = (EditText) findViewById(R.id.NachnameInput);
        String lastname = lastnameinput.getText().toString();

        Log.e("ERROR", lastname);

        // Request to server


        //

        finish();
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

                // Set new layout for entering user information

                setContentView(R.layout.activity_init2);



            }
            if(resultCode == RESULT_CANCELED){
                AlertDialog something = new AlertDialog.Builder(InitActivity.this).create();
                something.setMessage("Scanvorgang wurde abgebrochen!");
                something.show();
            }
        }
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
//                    final Response response = service.createUser(
//                            new LoginCredentials("lol@lol.de", "lol")
//                    )
//                            .execute();
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
