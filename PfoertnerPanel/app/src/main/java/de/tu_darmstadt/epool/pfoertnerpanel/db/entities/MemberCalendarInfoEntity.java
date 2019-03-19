package de.tu_darmstadt.epool.pfoertnerpanel.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.threeten.bp.LocalDateTime;

import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;

/**
 * This class holds the relevant calendar info for an office member.
 * This class will have a mapping SQLite table in the database.
 */
@Entity
public class MemberCalendarInfoEntity implements MemberCalendarInfo {
    @PrimaryKey
    private int memberId;

    private String calendarId;
    private String serverAuthCode;
    private String oAuthToken;
    private String eMail;
    private LocalDateTime created;
    private long oauth2TtlMinutes;
    private String refreshToken;
    private LocalDateTime webhookExpiration;

    public static MemberCalendarInfo toInterface(MemberCalendarInfoEntity entity){
        return entity;
    }

    public MemberCalendarInfoEntity(final int memberId) {
        this.memberId = memberId;
    }

    @Ignore
    public MemberCalendarInfoEntity(
            int memberId,
            String calendarId,
            String serverAuthCode,
            String oAuthToken,
            String eMail,
            LocalDateTime created,
            long oauth2TtlMinutes,
            String refreshToken,
            LocalDateTime webhookExpiration)
    {
        this.calendarId = calendarId;
        this.memberId = memberId;
        this.serverAuthCode = serverAuthCode;
        this.oAuthToken = oAuthToken;
        this.eMail = eMail;
        this.created = created;
        this.oauth2TtlMinutes = oauth2TtlMinutes;
        this.refreshToken = refreshToken;
        this.webhookExpiration = webhookExpiration;
    }

    @Override
    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    @Override
    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(final String calendarId) {
        this.calendarId = calendarId;
    }

    @Override
    public String getServerAuthCode() {
        return serverAuthCode;
    }

    public void setServerAuthCode(final String matchingServerAuthCode) {
        this.serverAuthCode = matchingServerAuthCode;
    }

    @Override
    public String getOAuthToken() {
        return oAuthToken;
    }

    public void setOAuthToken(String oAuthToken) {
        this.oAuthToken = oAuthToken;
    }

    @Override
    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    @Override
    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public long getOauth2TtlMinutes() {
        return oauth2TtlMinutes;
    }

    public void setOauth2TtlMinutes(long oauth2TtlMinutes) {
        this.oauth2TtlMinutes = oauth2TtlMinutes;
    }

    @Override
    public LocalDateTime getWebhookExpiration(){
        return webhookExpiration;
    }

    public void setWebhookExpiration(LocalDateTime webhookExpiration){
        this.webhookExpiration = webhookExpiration;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public MemberCalendarInfoEntity deepCopy(){
        return new MemberCalendarInfoEntity(
                getMemberId(),
                getCalendarId(),
                getServerAuthCode(),
                getOAuthToken(),
                getEMail(),
                getCreated(),
                getOauth2TtlMinutes(),
                getRefreshToken(),
                getWebhookExpiration()
        );
    }
}
