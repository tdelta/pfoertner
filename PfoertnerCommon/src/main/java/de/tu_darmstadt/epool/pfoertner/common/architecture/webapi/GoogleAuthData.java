package de.tu_darmstadt.epool.pfoertner.common.architecture.webapi;

/**
 * Body of the API call that passes an auth code to the server to authorize Google Calendar access.
 */
public class GoogleAuthData {
    /**
     * One time use auth code that can be used to retrieve a refresh token and an authorization token.
     */
    private final String serverAuthCode;


    public GoogleAuthData(final String serverAuthCode) {
        this.serverAuthCode = serverAuthCode;
    }
}
