package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import java.io.IOException;
import android.content.SharedPreferences;

import retrofit2.Call;

public class User {
    public final int id;

    public User(final int id) {
        this.id = id;
    }

    public static User loadDevice(final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Password password) {
        User device;

        if (deviceRegistrationInfo.contains("UserId") /*device already registered*/) {
            device = new User(deviceRegistrationInfo.getInt("UserId", -1));
        }

        else {
            // Create user
            try {
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
