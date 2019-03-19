package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import java.io.IOException;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.annotations.Expose;

import retrofit2.Call;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

/**
 * Body of a response of a create user request from the server. The id is later used for authentication.
 */
public class User {
    @Expose public final int id;

    public User(final int id) {
        this.id = id;
    }

    /**
     * Loads a device id from local settings or if none exists, makes a <b>blocking</b> request to the server, to create an account.
     * @param deviceRegistrationInfo local settings (PfoertnerApplication.getSettings())
     * @param service retrofit instance to communicate with the server
     * @param password password for account creation, not used if an account exists
     * @throws RuntimeException if the connection to the server fails
     * @return
     */
    public static User loadDevice(final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Password password) {
        User device;

        if (deviceRegistrationInfo.contains("UserId") /*device already registered*/) {
            device = new User(deviceRegistrationInfo.getInt("UserId", -1));
        }

        else {
            // Create user
            try {
                Log.d("CreateUser","CreateUser");
                final Call<User> deviceCall = service.createUser(password);
                device = deviceCall.execute().body();

                if (device != null) {
                    final SharedPreferences.Editor e = deviceRegistrationInfo.edit();
                    e.putInt("UserId", device.id);
                    e.apply();
                }
            }

            catch (final IOException e) {
                e.printStackTrace();
                device = null;
                // the if below will handle further steps
            }
        }

        if (device == null) {
            throw new RuntimeException("Could not register this device at the server. Do you have an internet connection?");
        }

        return device;
    }
}
