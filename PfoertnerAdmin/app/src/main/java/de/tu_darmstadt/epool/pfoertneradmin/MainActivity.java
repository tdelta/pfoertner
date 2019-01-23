package de.tu_darmstadt.epool.pfoertneradmin;


import android.accounts.AuthenticatorException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;


public class MainActivity extends AppCompatActivity implements TextFragment.TextDialogListener, StatusFragment.StatusDialogListener {
    private static final String TAG = "PfoertnerAdmin_MainActivity";

    private SharedPreferences settings;
    private PfoertnerService service;
    private StatusFragment globalStatusMenu;
    private State state = State.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create retrofit client
        service =  State.getInstance().service;

        settings = getSharedPreferences("Settings", 0);

        globalStatusMenu = StatusFragment.newInstance(this);


        // "Proof of concept" for persistence variable in memory
        if (settings.getInt("OfficeId",-1 ) == -1){

            Intent intent = new Intent(this, InitActivity.class);
            startActivity(intent);


        } else {
            new RequestTask<Authentication>(){
                @Override
                protected Authentication doRequests(){
                    Log.d(TAG, "Loading auth token...");
                    final Authentication authtoken = Authentication.authenticate(
                            settings,
                            service,
                            User.loadDevice(settings, service, Password.loadPassword(settings)),
                            Password.loadPassword(settings),
                            MainActivity.this
                    );

                    Log.d(TAG, "Got auth token.");

                    return authtoken;
                }

                @Override
                protected void onSuccess(final Authentication auth) {
                    State.getInstance().authtoken = auth;

                    Log.d(TAG, "Refreshed auth token of global State object.");
                }
            }.execute();
        }
    }

    public void editGlobalInfo(View view){
        globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");

    }

    public void gotoQRCodeAcitvity(View view) {
        Intent intent = new Intent(this, showQRCodeActivity.class);
        startActivity(intent);
    }

    public void getPicture(View view){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        Log.d(TAG, "in on result");
        // nur als test anzeige
        ImageView imagetest = (ImageView) findViewById(R.id.imageViewtest);

        if (reqCode == 1) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imagetest.setImageBitmap(selectedImage);

                //TODO: send picture to server

                Log.d(TAG, "pic loaded");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Something went wrong, with choosing a picture!", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(MainActivity.this, "You haven't picked Image!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void updateStatus(String text) {
        globalStatusMenu.updateStatus(text);
        globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");

    }

    @Override
    public void startTextInput() {
        TextFragment textBox = new TextFragment();
        textBox.show(getSupportFragmentManager(), "insertTextBox");
    }
}
