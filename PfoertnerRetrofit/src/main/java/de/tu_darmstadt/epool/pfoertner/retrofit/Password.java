package de.tu_darmstadt.epool.pfoertner.retrofit;

import java.util.UUID;

public class Password {
    public Password(final String password) {
        this.password = password;
    }

    public final String password;

    public Password loadPassword(final SharedPreferences deviceRegistrationInfo){

        if(deviceRegistrationInfo.contains("Password")){
            return new Password(
                deviceRegistrationInfo.getString("Password")
            );
        } else {
          // Generate a cryptographically strong random String
          String password UUID.randomUUID().toString();
          // Persist to storage
          final SharedPreferences.Editor e = deviceRegistrationInfo.edit();
          e.putString("Password", password);
          e.apply();

          return new Password(password);
        }
    }
}
