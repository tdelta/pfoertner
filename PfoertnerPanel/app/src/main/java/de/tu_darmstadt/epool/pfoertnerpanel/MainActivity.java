package de.tu_darmstadt.epool.pfoertnerpanel;

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

        // For now, this is a simple test of QR code generation for an arbitrary text.
        final ImageView image = findViewById(R.id.imageView);
        final TextView text = findViewById(R.id.editText);
        final Button button = findViewById(R.id.button);

        // Whenever the set button is pressed, generate and render a QR code for the text entered by the user
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Drawable qrCode = new QRCode(text.getText().toString());

                image.setImageDrawable(qrCode);
            }
        });
    }
}
