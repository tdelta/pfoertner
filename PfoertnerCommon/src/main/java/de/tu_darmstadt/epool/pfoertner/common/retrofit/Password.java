package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.SharedPreferences;

import com.google.gson.annotations.Expose;

import java.util.UUID;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

public class Password {
    public Password(final String password) {
        this.password = password;
    }

    @Expose public final String password;

    public static Password loadPassword(final SharedPreferences deviceRegistrationInfo){
        if(deviceRegistrationInfo.contains("Password")){
            return new Password(
                deviceRegistrationInfo.getString("Password", "")
            );
        }

        else {
          // Generate a cryptographically strong random String
          final String password = UUID.randomUUID().toString();

          // Persist to storage
          final SharedPreferences.Editor e = deviceRegistrationInfo.edit();
          e.putString("Password", password);
          e.apply();

          return new Password(password);
        }
    }
}
