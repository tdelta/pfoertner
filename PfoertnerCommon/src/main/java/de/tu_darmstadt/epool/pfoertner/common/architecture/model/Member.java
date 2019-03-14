package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

public interface Member {
    int getId();
    int getOfficeId();
    String getFirstName();
    String getLastName();
    String getStatus();
    String getPicture();
    String getPictureMD5();
    String getServerAuthCode();
    String getCalendarId();
    String getEmail();
    String getWebhookId();
}
