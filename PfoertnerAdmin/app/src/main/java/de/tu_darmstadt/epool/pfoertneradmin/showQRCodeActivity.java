package de.tu_darmstadt.epool.pfoertneradmin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;

public class showQRCodeActivity extends AppCompatActivity {
    private static final String TAG = "showQRCodeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qrcode);

        showQRCode();
    }

    protected void showQRCode(){
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        app
                .getRepo()
                .getOfficeRepo()
                .getOffice(app.getOffice().getId())
                .observe(this, office -> {
                    final ImageView qrCodeView = findViewById(R.id.qrCodeView);
                    final QRCode qrCode = new QRCode(
                            new QRCodeData(office).serialize()
                    );

                    qrCodeView.setImageDrawable(qrCode);
                });
    }
}
