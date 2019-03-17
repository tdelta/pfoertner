package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.Expose;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

/**
 * Marshalling class, used for api calls and for saving the authentication info in local settings
 */
public class Authentication {
    @Expose public final String id;
    @Expose public final int ttl;
    @Expose public final String created;
    @Expose public final int userId;

    public Authentication(
            final String id,
            final int ttl,
            final String created,
            final int userId
    ) {
        this.id = id;
        this.ttl = ttl;
        this.created = created;
        this.userId = userId;
    }

    /**
     * Checks if the authentication token has expired
     * @param context Unused
     * @return true if created plus ttl is after the current time
     */
    public boolean hasExpired(final Context context) {

        final LocalDateTime creationDate = LocalDateTime.parse(created, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        final LocalDateTime now = LocalDateTime.now();
        final Duration timePassed = Duration.between(creationDate, now);

        final Duration timeToLive = Duration.ofSeconds(this.ttl);

        // has more time passed, than the ttl? (with a safety margin of 1h)
        return timePassed.plus(Duration.ofHours(1)).compareTo(timeToLive) > 0;
    }

    /**
     * Makes a <b>blocking</b> call to the server to retrieve an authentication token,
     * if the current token has expired or no token exists. Saves the new token into local settings
     * @param deviceRegistrationInfo Local settings used for loading and saving authentication info
     * @param service Server API
     * @param user User id used for login
     * @param password Password used for login
     * @param context Unused
     * @throws RuntimeException if the server call fails
     * @return Authentication instance with a valid token
     */
    public static Authentication authenticate(
            final SharedPreferences deviceRegistrationInfo,
            final PfoertnerService service,
            final User user,
            final Password password,
            final Context context
    ) {
        Authentication auth = null;

        if (deviceRegistrationInfo.contains("AuthenticationId") /*auth already saved*/) {
            auth = new Authentication(
                    deviceRegistrationInfo.getString("AuthenticationId", ""),
                    deviceRegistrationInfo.getInt("AuthenticationTtl", -1),
                    deviceRegistrationInfo.getString("AuthenticationCreated", ""),
                    deviceRegistrationInfo.getInt("AuthenticationUserId", -1)
            );
        }

        if (auth == null || auth.hasExpired(context)) {
            try {
                auth = service
                        .login(user.id, new LoginCredentials(password.password))
                        .execute()
                        .body();

                if (auth != null) {
                    // save newly created Authentication
                    final SharedPreferences.Editor e = deviceRegistrationInfo.edit();

                    e.putString("AuthenticationId", auth.id);
                    e.putInt("AuthenticationTtl", auth.ttl);
                    e.putString("AuthenticationCreated", auth.created);
                    e.putInt("AuthenticationUserId", auth.userId);

                    e.apply();
                }
            }

            catch (final IOException e) {
                e.printStackTrace();
                auth = null;
                // the if below will handle further steps
            }
        }

        if (auth == null) {
            throw new RuntimeException("Could not authenticate at the server. Do you have an internet connection?");
        }

        return auth;
    }
}
