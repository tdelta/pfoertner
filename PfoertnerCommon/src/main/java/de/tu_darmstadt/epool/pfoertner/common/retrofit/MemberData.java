package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import android.content.SharedPreferences;

import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class MemberData {
    public final int id;
    public final String lastName;
    public final String firstName;
    public final String status;

    public MemberData(final int id, final String firstName, final String lastName, final String status){
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.status = status;
    //    "id":13,"firstName":"gsdg","lastName":"dhdh","status":null,"email":null,"picture":null,"createdAt":"2019-01-30T08:20:25.316Z","updatedAt":"2019-01-30T08:20:25.454Z","DeviceId":23,"OfficeId":9
    }
}
