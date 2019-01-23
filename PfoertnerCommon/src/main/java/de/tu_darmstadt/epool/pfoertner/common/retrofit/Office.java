package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

public class Office {
  final public int id;
  final public String joinCode;
  public String status;

  public Office(int id, String joinCode) {
    this.id = id;
    this.joinCode = joinCode;
  }

  public static Office createOffice(final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Authentication auth) {
    Office office;

    if (deviceRegistrationInfo.contains("OfficeId") /*office already registered*/) {
      office = new Office(
              deviceRegistrationInfo.getInt("OfficeId", -1),
              deviceRegistrationInfo.getString("OfficeJoinData", "")
      );
    }

    else {
      // Create office
      try {
        office = service
                .createOffice(auth.id)
                .execute()
                .body();

        if (office != null) {Log.d("DEBUG", "vor api call");
          final SharedPreferences.Editor e = deviceRegistrationInfo.edit();
          e.putInt("OfficeId", office.id);
          e.putString("OfficeJoinData", office.joinCode);
          e.apply();
        }
      }

      catch (final IOException e) {
        e.printStackTrace();
        office = null;
        // the if below will handle further steps
      }
    }

    if (office == null) {
      throw new RuntimeException("Could not create a new office. Do you have an internet connection?");
    }

    return office;
  }

  public static Office loadOffice(final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Authentication auth) {
    final int officeID = deviceRegistrationInfo.getInt("OfficeId", -1);
    if (officeID == -1){
      throw new RuntimeException("Office could not be loaded. Invalid officeId was loaded.");
    }

    return Office.loadOffice(
            officeID,
            deviceRegistrationInfo,
            service,
            auth
    );
  }

  public static Office loadOffice(final int officeId, final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Authentication auth) {
    Office office;

    try {
       office = service
               .loadOffice(auth.id, officeId)
                .execute()
                .body();

        if (office != null) {Log.d("DEBUG", "vor api call");
          final SharedPreferences.Editor e = deviceRegistrationInfo.edit();

          e.putInt("OfficeId", office.id);
          e.putString("OfficeJoinData", office.joinCode);

          e.apply();
        }
      }

      catch (final IOException e) {
        e.printStackTrace();
        office = null;
        // the if below will handle further steps
      }

    if (office == null) {
      Log.d("Office", "Had to load office from local storage since we could not connect.");

      office = new Office(
              deviceRegistrationInfo.getInt("OfficeId", -1),
              deviceRegistrationInfo.getString("OfficeJoinData", "")
      );
    }

    return office;
  }

  public static Person joinOffice(final int officeId, final String joinCode, String firstName, String lastName, SharedPreferences settings, PfoertnerService service, Authentication authtoken)  {
    try{
      final Person person = service.joinOffice(
            authtoken.id,
            officeId,
            new OfficeJoinData(
              joinCode,
              firstName,
              lastName
            )
      ).execute().body();

      // TODO save Person in preferences

      return person;
    }

    catch(final IOException e) {
      e.printStackTrace();

      throw new RuntimeException("Could not join office. Do you have an internet connection?");
    }
  }

  public static boolean hadBeenRegistered(final SharedPreferences settings) {
      return settings.contains("OfficeId");
  }
}
