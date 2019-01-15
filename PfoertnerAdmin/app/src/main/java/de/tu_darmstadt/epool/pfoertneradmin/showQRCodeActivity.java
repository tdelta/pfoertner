package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;

public class showQRCodeActivity extends AppCompatActivity {

    private PfoertnerService service;
    private SharedPreferences settings;
    private State state = State.getInstance();
    private Authentication authtoken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qrcode);

        service = state.service;
        settings = getSharedPreferences("Settings",0);
        authtoken = state.authtoken;

        showQRCode();
    }

    protected void showQRCode(){
        final Context self = this;

        new RequestTask<Office>(){

            Office office;

            @Override
            protected Office doRequests(){
                //return new Office(1,"9Our4FiGPCy2CcdkyjHMPlzVM1nTkHVz");
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
