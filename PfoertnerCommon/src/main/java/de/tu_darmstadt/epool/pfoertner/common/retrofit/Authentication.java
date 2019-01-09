package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;

public class Authentication {
    public final String id;
    public final int ttl;
    public final String created;
    public final int userId;

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

    public boolean hasExpired(final Context context) {
        AndroidThreeTen.init(context);

        final LocalDateTime creationDate = LocalDateTime.parse(created, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        final LocalDateTime now = LocalDateTime.now();
        final Duration timePassed = Duration.between(creationDate, now);

        final Duration timeToLive = Duration.ofSeconds(this.ttl);

        // has more time passed, than the ttl? (with a safety margin of 1h)
        return timePassed.plus(Duration.ofHours(1)).compareTo(timeToLive) > 0;
    }

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
                        .login(new LoginCredentials(password.password, user.id))
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
