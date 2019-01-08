package de.tu_darmstadt.epool.pfoertneradmin.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.retrofit.LoginCredentials;
import de.tu_darmstadt.epool.pfoertner.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.retrofit.User;
import de.tu_darmstadt.epool.pfoertneradmin.State;

public class InitTask extends AsyncTask<Void, Void, Void> {

    private PfoertnerService service;
    private SharedPreferences settings;


    public InitTask(final PfoertnerService service, SharedPreferences settings){
        this.service = service;
        this.settings = settings;
    }

    @Override
    protected Void doInBackground(final Void ... parameters){

        //TODO: Somehow peusdo random create later
        final Password password = new Password("GEHEIM!");

        // Save the password
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("password", password.password);

        try {
            // First api call
            final User device = service.createUser(password).execute().body();
            Log.d("RESULT", "ID: " +device.id);


            // Save the DeviceID
            editor.putInt("deviceID", device.id);

            // Create logincredentials with the generated password and the id from the server
            final LoginCredentials logincredentials = new LoginCredentials(password.password, device.id);

            // Second api call
            final Authentication authtoken = service.login(logincredentials).execute().body();
            Log.d("RESULT", "ID: " +authtoken.id);
            Log.d("RESULT", "UserID: " +authtoken.userId);

            // Update the static State, because we want to access the Authentoken in
            // MainActivity
            State.getInstance().authtoken = authtoken;

        }
        // TODO: Think about resoanable exception handling
        catch (IOException e) {
            e.printStackTrace();
        }
        // Commit changes of password and userid to the persistend memory
        editor.commit();
        return null;
    }
}