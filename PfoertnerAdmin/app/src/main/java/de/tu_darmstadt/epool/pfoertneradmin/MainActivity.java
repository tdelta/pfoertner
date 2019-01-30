package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;


public class MainActivity extends AppCompatActivity implements GlobalTextFragment.TextDialogListener, GlobalStatusFragment.StatusDialogListener {
    private static final String TAG = "PfoertnerAdmin_MainActivity";
    private GlobalStatusFragment globalStatusMenu;
    private PersonalStatusFragment personalStatusMenu;

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
    }

    private void onInitialized() {
        MainActivity.this.startService(
                new Intent(MainActivity.this, SyncService.class)
        );

        globalStatusMenu = GlobalStatusFragment.newInstance(this);
        personalStatusMenu = PersonalStatusFragment.newInstance(this);
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


    public void editPersonalStatus(View view){
        if (personalStatusMenu != null) {
            personalStatusMenu.show(getSupportFragmentManager(), "personalStatusMenu");
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
    public void updateGlobalStatus(String text) {
        if (globalStatusMenu != null) {
            globalStatusMenu.updateStatus(text);
            globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");
        }
    }

    @Override
    public void updatePersonalStatus(String text){
        if (personalStatusMenu != null){
            personalStatusMenu.updateStatus(text);
            personalStatusMenu.show(getSupportFragmentManager(), "personalStatusMenu");
        }
    }


    @Override
    public void startGlobalTextInput() {
        GlobalTextFragment textBox = new GlobalTextFragment();
        textBox.show(getSupportFragmentManager(), "insertTextBox");
    }

    @Override
    public void startPersonalTextInput(){
        PersonalTextFragment textBox = new PersonalTextFragment();
        textBox.show(getSupportFragmentManager(), "insertTextBox");
    }
}
