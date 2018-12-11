package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertnerpanel.qrcode.QRCode;

public class MainActivity extends AppCompatActivity {

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
    }
}
