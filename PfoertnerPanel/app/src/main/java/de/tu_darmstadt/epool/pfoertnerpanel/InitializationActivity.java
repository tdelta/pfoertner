package de.tu_darmstadt.epool.pfoertnerpanel;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import de.tu_darmstadt.epool.pfoertnerpanel.qrcode.QRCode;

public class InitializationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_initialization);

        final ImageView qrCodeView = findViewById(R.id.qrCodeView);
        final Drawable qrCode = new QRCode("https://i.kym-cdn.com/photos/images/newsfeed/001/091/264/665.jpg");

        qrCodeView.setImageDrawable(qrCode);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar.
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Hide action bar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
}
