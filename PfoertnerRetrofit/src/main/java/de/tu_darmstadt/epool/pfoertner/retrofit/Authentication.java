package de.tu_darmstadt.epool.pfoertner.retrofit;

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

    public static Authentication authenticate(final PfoertnerService service, final User user, final Password password) {
        Authentication auth;

        try {
            auth = service
                    .login(new LoginCredentials(password.password, user.id))
                    .execute()
                    .body();
        }

        catch (final IOException e) {
            e.printStackTrace();
            auth = null;
            // the if below will handle further steps
        }

        if (auth == null) {
            throw new RuntimeException("Could not authenticate at the server.");
        }

        return auth;
    }
}
