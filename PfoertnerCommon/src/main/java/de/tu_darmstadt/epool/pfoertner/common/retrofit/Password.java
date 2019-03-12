package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.SharedPreferences;

import com.google.gson.annotations.Expose;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

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
          final PasswordGenerator passayGen = new PasswordGenerator();

          final String password = passayGen.generatePassword(
                  80,
                  new CharacterRule(EnglishCharacterData.UpperCase, 15),
                  new CharacterRule(EnglishCharacterData.LowerCase, 20),
                  new CharacterRule(EnglishCharacterData.Digit, 10),
                  new CharacterRule(EnglishCharacterData.Special, 10)
          );

          // Persist to storage
          final SharedPreferences.Editor e = deviceRegistrationInfo.edit();
          e.putString("Password", password);
          e.apply();

          return new Password(password);
        }
    }
}
