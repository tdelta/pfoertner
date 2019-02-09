package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

public class MemberData {
    @Expose public int id;
    @Expose public String lastName;
    @Expose public String firstName;
    @Expose public String pictureMD5;
    @Expose public String status;
    @Expose public String serverAuthCode;
    @Expose public String calendarId;
    public String oauthToken;
}
