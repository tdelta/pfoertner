package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;


import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;

import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;

public class InitializationActivity extends AppCompatActivity {
    private void initPanel(final Context context, final Consumer<Void> closeSplashScreen) {
        final PfoertnerService service = PfoertnerService.makeService("http://deh.duckdns.org:3000/api/");
        final SharedPreferences registrationInfo = context.getSharedPreferences("registrationInfo", MODE_PRIVATE);

        new RequestTask<Office>() {
            @Override
            protected Office doRequests() {
                final Password password = Password.loadPassword(registrationInfo);
                final User device = User.loadDevice(registrationInfo, service, password);
                final Authentication authToken = Authentication.authenticate(registrationInfo, service, device, password, context);
                final Office office = Office.createOffice(registrationInfo, service, authToken);

                return office;
            }

            @Override
            protected void onSuccess(Office result) {
                showQRCode(result);

                closeSplashScreen.accept(null);
            }

            @Override
            protected void onException(Exception e) {
                ErrorInfoDialog.show(context, e.getMessage(), aVoid -> initPanel(context, closeSplashScreen));
            }
        }.execute();
    }

    private void showQRCode(final Office office) {
        final ImageView qrCodeView = findViewById(R.id.qrCodeView);
        final QRCode qrCode = new QRCode(
                new QRCodeData(office).serialize()
        );

        qrCodeView.setImageDrawable(qrCode);
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
