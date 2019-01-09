package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

public class Office {
  final public int id;
  final public String userJoinCode;

  public Office(int id, String userJoinCode) {
    this.id = id;
    this.userJoinCode = userJoinCode;
  }

  public static Office createOffice(final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Authentication auth) {
    Office office;

    if (deviceRegistrationInfo.contains("OfficeId") /*office already registered*/) {
      office = new Office(
              deviceRegistrationInfo.getInt("OfficeId", -1),
              deviceRegistrationInfo.getString("OfficeJoinCode", "")
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
          e.putString("OfficeJoinCode", office.userJoinCode);
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
      throw new RuntimeException("Could not create a new office.");
    }

    return office;
  }

  public static Office loadOffice(final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Authentication auth) {
    Office office;

    if (deviceRegistrationInfo.contains("OfficeId") /*office already registered*/) {
      office = new Office(
              deviceRegistrationInfo.getInt("OfficeId", -1),
              deviceRegistrationInfo.getString("OfficeJoinCode", "")
      );
    }

    else {
      // Create office
      try {
        office = service
                .loadOffice(auth.id)
                .execute()
                .body();

        if (office != null) {Log.d("DEBUG", "vor api call");
          final SharedPreferences.Editor e = deviceRegistrationInfo.edit();
          e.putInt("OfficeId", office.id);
          e.putString("OfficeJoinCode", office.userJoinCode);
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
      throw new RuntimeException("Could not create a new office.");
    }

    return office;
  }

  public static void joinOffice(PfoertnerService service, Authentication authtoken, Office office)  {

    try{
      Log.d("DEBUG", "vor api call");
      Log.d("DEBUG", "" + authtoken.userId);
      Log.d("DEBUG", "" + authtoken.id);
      Log.d("DEBUG", "" + office.id);
      Log.d("DEBUG", "" + office.userJoinCode);

      Log.d("DEBUG", "" + service.joinOffice(authtoken.id, office.id, new OfficeJoinCode(office.userJoinCode)).execute().code());
    }
    catch(final IOException e){
      e.printStackTrace();

      throw new RuntimeException("Could not join office. Do you have an internet connection?");
    }
  }
}
