package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("Settings", 0);

        // "Proof of concept" for persistence variable in memory
        if (settings.getString("token",null ) == null){

            Intent intent = new Intent(this, InitActivity.class);
            startActivity(intent);

        } else{
            AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
            something.setMessage("Die App wurde bereits mit einem Token initialisiert. Willkommen im Startbereich.");
            something.show();
        }

        AlertDialog something = new AlertDialog.Builder(MainActivity.this).create();
        something.setMessage("Hier wird wieder eingestiegen.");
        something.show();
    }
}
