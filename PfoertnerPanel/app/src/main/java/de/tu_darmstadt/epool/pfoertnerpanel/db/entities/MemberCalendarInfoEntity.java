package de.tu_darmstadt.epool.pfoertnerpanel.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;

@Entity
public class MemberCalendarInfoEntity implements MemberCalendarInfo {
    @PrimaryKey
    private int memberId;

    private String calendarId;
    private String serverAuthCode;
    private String oAuthToken;
    private String eMail;

    public MemberCalendarInfoEntity(int memberId, String calendarId, String serverAuthCode, String oAuthToken, String eMail) {
        this.calendarId = calendarId;
        this.memberId = memberId;
        this.serverAuthCode = serverAuthCode;
        this.oAuthToken = oAuthToken;
        this.eMail = eMail;
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
}
