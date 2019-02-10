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

    public MemberData(final int id, final String firstName, final String lastName, final String pictureMD5, final String status){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pictureMD5 = pictureMD5;
        this.status = status;
    }
}
