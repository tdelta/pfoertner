package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.SharedPreferences;

import com.google.gson.annotations.Expose;

import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class MemberData {
    @Expose public final int id;
    @Expose public final String lastName;
    @Expose public final String firstName;
    @Expose public final String status;
    @Expose public String serverAuthCode;
    public String accessToken;


    public MemberData(final int id, final String firstName, final String lastName, final String status, final String accessToken){
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.status = status;
        this.accessToken = accessToken;
        // "id":13,"firstName":"gsdg","lastName":"dhdh","status":null,"email":null,"picture":null,"createdAt":"2019-01-30T08:20:25.316Z","updatedAt":"2019-01-30T08:20:25.454Z","DeviceId":23,"OfficeId":9
    }
}
