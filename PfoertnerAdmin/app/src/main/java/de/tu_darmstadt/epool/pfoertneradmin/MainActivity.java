package de.tu_darmstadt.epool.pfoertneradmin;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Office;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;


public class MainActivity extends AppCompatActivity implements TextFragment.TextDialogListener, StatusFragment.StatusDialogListener {
    private static final String TAG = "PfoertnerAdmin_MainActivity";
    private StatusFragment globalStatusMenu;

    public void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (!Office.hadBeenRegistered(app.getSettings())){
            Intent intent = new Intent(this, InitActivity.class);
            startActivity(intent);
        } else {
            new RequestTask<Authentication>(){
                @Override
                protected Authentication doRequests(){
                    app.init();

                    return null;
                }

                @Override
                protected void onException(Exception e){
                    ErrorInfoDialog.show(MainActivity.this, e.getMessage(), aVoid -> init());
                }
            }.execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globalStatusMenu = StatusFragment.newInstance(this);

        init();
    }

    public void editGlobalInfo(View view){
        globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");

    }

    public void gotoQRCodeAcitvity(View view) {
        Intent intent = new Intent(this, showQRCodeActivity.class);
        startActivity(intent);
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
