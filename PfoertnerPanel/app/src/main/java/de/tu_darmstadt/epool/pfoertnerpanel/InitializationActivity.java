package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.spencerwi.either.Either;

import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.retrofit.Password;
import de.tu_darmstadt.epool.pfoertnerpanel.qrcode.QRCode;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import de.tu_darmstadt.epool.pfoertner.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.retrofit.OfficeJoinInfo;
import de.tu_darmstadt.epool.pfoertner.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.retrofit.User;

public class InitializationActivity extends AppCompatActivity {
    private void initPanel(final Context context, final Consumer<Void> closeSplashScreen) {
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
                context.getSharedPreferences("registrationInfo", MODE_PRIVATE),
                service,
                e -> this.showQRCode(context, closeSplashScreen, e)
        ).execute();
    }

    private static class InitTask extends AsyncTask<Void, Void, Either<String, OfficeJoinInfo>> {
        private final SharedPreferences registrationInfo;
        private final PfoertnerService service;
        private final Consumer<Either<String, OfficeJoinInfo>> callback;

        InitTask(final SharedPreferences registrationInfo, final PfoertnerService service, final Consumer<Either<String, OfficeJoinInfo>> callback) {
            this.registrationInfo = registrationInfo;
            this.service = service;
            this.callback = callback;
        }

        @Override
        protected Either<String, OfficeJoinInfo> doInBackground(final Void ... parameters) {
            try {
                final Password password = new Password("lol");
                final User device = User.loadDevice(registrationInfo, service, password);
                final Authentication authToken = Authentication.authenticate(service, device, password);
                final Office office = Office.loadOffice(registrationInfo, service, authToken);

                return Either.right(
                        OfficeJoinInfo.loadJoinCode(service, office, authToken)
                );
            }

            catch (final RuntimeException e) {
                e.printStackTrace();

                return Either.left(e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(final Either<String, OfficeJoinInfo> officeJoinInfo) {
            callback.accept(officeJoinInfo);
        }
    }

    private void showQRCode(final Context context, final Consumer<Void> closeSplashScreen, final Either<String, OfficeJoinInfo> data) {
        data.run(
                errorInformation -> {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                    alertDialogBuilder.setMessage(
                        "We encountered a problem:\n\n" +
                                errorInformation
                    );

                    alertDialogBuilder.setPositiveButton(
                        "Retry",
                        (dialog, which) -> initPanel(context, closeSplashScreen)
                    );

                    alertDialogBuilder.setCancelable(false);

                    alertDialogBuilder.show();
                },

                joinInfo -> {
                    final ImageView qrCodeView = findViewById(R.id.qrCodeView);
                    final QRCode qrCode = new QRCode(joinInfo.joinCode);

                    qrCodeView.setImageDrawable(qrCode);

                    closeSplashScreen.accept(null);
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_initialization);

        SplashScreenActivity.run(this,
            this::initPanel
        );
    }

    @Override
    public void onBackPressed() {
        // prevent the user from removing the initialization activity from the activity stack. (pressing back button)
        // instead, move the entire app into the background
        moveTaskToBack(true);
    }
}
