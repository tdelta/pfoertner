package de.tu_darmstadt.epool.pfoertnerpanel.models;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

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

    default boolean oauthTokenHasExpired(){
        if(getCreated()==null) return false;
        final LocalDateTime now = LocalDateTime.now();
        final Duration passedTime = Duration.between(getCreated(), now);

        final Duration expirationTime = Duration.ofMinutes(getOauth2TtlMinutes());
        return expirationTime.compareTo(passedTime) < 0;
    }

    default boolean webhookHasExpired(){
        if(getWebhookExpiration() == null) return false;
        return getWebhookExpiration().isBefore(LocalDateTime.now());
    }
}
