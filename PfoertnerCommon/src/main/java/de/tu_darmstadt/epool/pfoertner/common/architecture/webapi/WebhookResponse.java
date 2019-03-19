package de.tu_darmstadt.epool.pfoertner.common.architecture.webapi;

/**
 * JSON body of a response to a webhook request from google
 * https://developers.google.com/calendar/v3/push
 */
public class WebhookResponse {
    private long expiration;

    public long getExpiration() {
        return expiration;
    }
}
