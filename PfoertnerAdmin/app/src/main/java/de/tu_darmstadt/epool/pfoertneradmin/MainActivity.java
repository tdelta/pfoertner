package de.tu_darmstadt.epool.pfoertneradmin;


import android.Manifest;
import android.accounts.AuthenticatorException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;


import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.CalendarService;
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;


public class MainActivity extends AppCompatActivity implements TextFragment.TextDialogListener, StatusFragment.StatusDialogListener {
    private static final String TAG = "PfoertnerAdmin_MainActivity";
    private final static int MY_PERMISSIONS_READ_CALENDAR = 1;
    private StatusFragment globalStatusMenu;
    private StatusFragment ownStatusMenu;

    private void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (!Office.hadBeenRegistered(app.getSettings())) {
            Intent intent = new Intent(this, InitActivity.class);
            MainActivity.this.startActivityForResult(intent, 0);
        } else {
            new RequestTask<Void>(){
                @Override
                protected Void doRequests(){
                    app.init();
                    return null;
                }

                @Override
                protected void onSuccess(Void result) {
                    MainActivity.this.onInitialized();
                }

                @Override
                protected void onException(Exception e){
                    ErrorInfoDialog.show(MainActivity.this, e.getMessage(), aVoid -> init());
                }
            }.execute();
        }

        startCalenderService();
    }

    private void onInitialized() {
        MainActivity.this.startService(
                new Intent(MainActivity.this, SyncService.class)
        );

        globalStatusMenu = StatusFragment.newInstance(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0) {
            onInitialized();
        }
    }

    public void editGlobalInfo(View view){
        if (globalStatusMenu != null) {
            globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");
        }
    }

    public void gotoQRCodeAcitvity(View view) {
        Intent intent = new Intent(this, showQRCodeActivity.class);
        startActivity(intent);
    }

    public void gotoPictureUploader(View view){
        Intent intent = new Intent(this, pictureUpload.class);
        startActivity(intent);
    }

    @Override
    public void updateStatus(String text) {
        if (globalStatusMenu != null) {
            globalStatusMenu.updateStatus(text);
            globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");
        }
    }

    @Override
    public void startTextInput() {
        TextFragment textBox = new TextFragment();
        textBox.show(getSupportFragmentManager(), "insertTextBox");
    }

    private void startCalenderService() {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {//Checking permission
                //Starting service for registering ContactObserver
                Intent intent = new Intent(this, CalendarService.class);
                startService(intent);
            } else {
                //Ask for READ_CALENDAR permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, MY_PERMISSIONS_READ_CALENDAR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If permission granted
        if (requestCode == MY_PERMISSIONS_READ_CALENDAR && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCalenderService();
        }
    }
}
