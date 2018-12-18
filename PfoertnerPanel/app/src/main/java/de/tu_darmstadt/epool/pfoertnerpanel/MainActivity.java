package de.tu_darmstadt.epool.pfoertnerpanel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;

import android.util.Log;

import de.tu_darmstadt.epool.pfoertnerpanel.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.PfoertnerService;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.User;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.LoginCredentials;

public class MainActivity extends AppCompatActivity {
    private LayoutInflater inflater;
    private ViewGroup container;
    private TableRow row;
    private int memberCount = 0;

    public enum GlobalStatus {
        DO_NOT_DISTURB, COME_IN, EXTENDED_ACCESS
    }

    @SuppressLint("StaticFieldLeak")
    private void testApi() {
        // Debug logging
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor).build();

        final Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl("http://172.18.84.214:3000")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        final PfoertnerService service = retrofit.create(PfoertnerService.class);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( final Void ... params ) {
                try {
                    final Response<User> response = service.createUser(
                            new LoginCredentials("lol", "lol@lol.de")
                    )
                            .execute();

                    final User user = response.body();

                    if (user == null) {
                        Log.d("MainActivity", "Request failed: " + response.message());
                    }

                    else {
                        Log.d("MainActivity", "Request successful: " + String.valueOf(user.id));
                    }
                }

                catch (final IOException e) {
                    Log.d("MainActivity", "trolololo");
                }

                return null;
            }

            @Override
            protected void onPostExecute( final Void result ) {
            }
        }.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // for now, immediately start initialization screen
        final Intent initIntent = new Intent(
                MainActivity.this,
                InitializationActivity.class
        );

        MainActivity.this.startActivity(initIntent);


        inflater =  getLayoutInflater();
        container = findViewById(R.id.member_insert);

        setRoom("S101/A1");
        setGlobalStatus(GlobalStatus.EXTENDED_ACCESS);

        testApi();
    }
    public void setRoom(String str){
        TextView room = findViewById(R.id.room);
        room.setText(str);
    }

    public void setGlobalStatus(MainActivity.GlobalStatus stat){
        TextView global = findViewById(R.id.global_status);
        switch (stat){
            case COME_IN:{
                global.setText(getString(R.string.come_in));
                global.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            }
            case DO_NOT_DISTURB:{
                global.setText(getString(R.string.do_not_disturb));
                global.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
                break;
            }
            case EXTENDED_ACCESS:{
                global.setText(getString(R.string.extended_access));
                global.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                global.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                break;
            }
        }

    }

    public void addMember(View view){

        // set the attributes of the member to add
        String[] work = {"Mo-Fr 8:00 - 23:00", "Sa-So 8:00 - 23:00"};
        Member member = new Member(this);
        member.setName("Prof. Dr. Ing. Max Mustermann");
        member.setStatus(Member.Status.OUT_OF_OFFICE);
        member.setOfficeHours(work);
        member.setImage(getDrawable(R.drawable.ic_contact_default));

        switch(memberCount){
            case 0:{
                row = (TableRow) inflater.inflate(R.layout.table_row, container, false);
                inflater.inflate(R.layout.space, row);
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                container.addView(row);
                memberCount++;
                break;
            }
            case 1:{
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                memberCount++;
                break;
            }
            case 2:{
                row = (TableRow) inflater.inflate(R.layout.table_row, container, false);
                inflater.inflate(R.layout.space, row);
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                container.addView(row);
                memberCount++;
                break;
            }
            case 3:{
                row.addView(member.getView());
                inflater.inflate(R.layout.space, row);
                memberCount++;
                break;
            }
        }
    }


}
