package de.tu_darmstadt.epool.pfoertnerpanel.models;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Provides methods to access calendar related information for an office member
 */
public interface MemberCalendarInfo {
    int getMemberId();

    LocalDateTime getCreated();
    String getRefreshToken();
    long getOauth2TtlMinutes();
    String getCalendarId();
    String getServerAuthCode();
    String getOAuthToken();
    String getEMail();
    LocalDateTime getWebhookExpiration();

    /**
     * checks if the oauthToken has expired
     * @return boolean depending if oauthtoken has expired
     */
    default boolean oauthTokenHasExpired(){
        if(getCreated()==null) return false;
        final LocalDateTime now = LocalDateTime.now();
        final Duration passedTime = Duration.between(getCreated(), now);

        final Duration expirationTime = Duration.ofMinutes(getOauth2TtlMinutes());
        return expirationTime.compareTo(passedTime) < 0;
    }

    /**
     * checks if the webhook has expired
     * @return boolean depending if the webhook has expired
     */
    default boolean webhookHasExpired(){
        if(getWebhookExpiration() == null) return false;
        return getWebhookExpiration().isBefore(LocalDateTime.now());
    }
}
