package de.tu_darmstadt.epool.pfoertnerpanel;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.Authentication;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.LoginCredentials;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.Office;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.OfficeJoinInfo;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.PfoertnerService;
import de.tu_darmstadt.epool.pfoertnerpanel.PfoertnerService.User;
import de.tu_darmstadt.epool.pfoertnerpanel.qrcode.QRCode;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InitializationActivity extends AppCompatActivity {
    private static User loadDevice(final PfoertnerService service, final LoginCredentials credentials) throws IOException {
        final User device;
        if (false /*device already registered*/) {
            device = null;
        }

        else {
            // Create user
            final Call<User> deviceCall = service.createUser(credentials);
            device = deviceCall.execute().body();

            if (device == null) {
                //TODO
            }
        }

        return device;
    }

    private static Authentication authenticate(final PfoertnerService service, final LoginCredentials credentials) throws IOException {
        final Authentication auth = service
                .login(credentials)
                .execute()
                .body();

        if (auth == null) {
            //TODO
        }

        return auth;
    }

    private static Office loadOffice(final PfoertnerService service, final Authentication auth) throws IOException {
        final Office office;

        if (false /*office already registered*/) {
            office = null;
        }

        else {
            // Create office
            office = service
                    .createOffice(auth.id)
                    .execute()
                    .body();

            if (office == null) {
                //TODO
            }
        }

        return office;
    }

    private static OfficeJoinInfo loadJoinCode(final PfoertnerService service, final Office office, final Authentication auth) {
        return new OfficeJoinInfo(office.id, "TODO"); // TODO
    }

    private void initPanel() {
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

        new InitTask(
                service,
                this::showQRCode
        ).execute();
    }

    private static class InitTask extends AsyncTask<Void, Void, OfficeJoinInfo> {
        private final PfoertnerService service;
        private final Consumer<OfficeJoinInfo> callback;

        InitTask(final PfoertnerService service, final Consumer<OfficeJoinInfo> callback) {
            this.service = service;
            this.callback = callback;
        }

        @Override
        protected OfficeJoinInfo doInBackground(final Void ... parameters) {
            try {
                final LoginCredentials credentials = new LoginCredentials("lol", "lol@lol.de");
                final User device = loadDevice(service, credentials);
                final Authentication authToken = authenticate(service, credentials);
                final Office office = loadOffice(service, authToken);

                return loadJoinCode(service, office, authToken);
            }

            catch(final IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final OfficeJoinInfo officeJoinInfo) {
            callback.accept(officeJoinInfo);
        }
    }

    private void showQRCode(final OfficeJoinInfo data) {
        final ImageView qrCodeView = findViewById(R.id.qrCodeView);
        final QRCode qrCode = new QRCode(data.joinCode);

        qrCodeView.setImageDrawable(qrCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_initialization);

        initPanel();
    }

    public void onClick(View v) {
        finish();
    }
}
