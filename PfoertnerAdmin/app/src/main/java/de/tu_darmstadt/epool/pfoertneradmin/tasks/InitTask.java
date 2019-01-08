package de.tu_darmstadt.epool.pfoertneradmin.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.spencerwi.either.Either;
import java.util.function.Consumer;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.LoginCredentials;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;
import de.tu_darmstadt.epool.pfoertneradmin.State;

public class InitTask extends AsyncTask<Void, Void, Either<String, Void>>{
    private Consumer<Either<String, Void>> callback;
    private PfoertnerService service;
    private SharedPreferences settings;


    public InitTask(final PfoertnerService service, SharedPreferences settings, final Consumer<Either<String, Void>> callback){
        this.service = service;
        this.settings = settings;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(Either<String, Void> stringVoidEither) {
        this.callback.accept(stringVoidEither);
    }

    @Override
    protected Either<String, Void> doInBackground(final Void ... parameters){




        try {
            // Save the password
            final Password password = Password.loadPassword(settings);

            // First api call

            final User device = User.loadDevice(settings, service, password);
            Log.d("RESULT", "ID: " +device.id);



            // Create logincredentials with the generated password and the id from the server
            final LoginCredentials logincredentials = new LoginCredentials(password.password, device.id);

            // Second api call
            final Authentication authtoken = Authentication.authenticate(service, device, password);
            Log.d("RESULT", "ID: " +authtoken.id);
            Log.d("RESULT", "UserID: " +authtoken.userId);

            // Update the static State, because we want to access the Authentoken in
            // MainActivity
            State.getInstance().authtoken = authtoken;

        }
        // TODO: Think about resoanable exception handling
        catch (Exception e) {
            e.printStackTrace();
            return Either.left(e.getMessage());
        }
        return Either.right(null);
    }
}