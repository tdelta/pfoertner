package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.Console;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //settings = getSharedPreferences("Settings", 0);

        //Log.e("Error", settings.getString("token", "Du bbist dumm"));

        //if (settings.getString("token",null ) != null){
        //    AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
        //    something.setMessage("Die App wurde bereits mit einem Token initialisiert");
        //    something.show();
        //}
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handel results from QR code scanner
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                String token = data.getStringExtra("SCAN_RESULT");
                AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
                something.setMessage(token);
                something.show();

                //SharedPreferences.Editor editor = settings.edit();
                //editor.putString("token", token);

            }
            if(resultCode == RESULT_CANCELED){
                AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
                something.setMessage("And it was at this moment, he knew, he fucked up");
                something.show();
            }
        }
    }
}
