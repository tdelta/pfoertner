package de.tu_darmstadt.epool.pfoertnerpanel.models;

public interface MemberCalendarInfo {
    int getMemberId();

    String getCalendarId();
    String getServerAuthCode();
    String getOAuthToken();
    String getEMail();
}
