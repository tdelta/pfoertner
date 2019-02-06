package de.tu_darmstadt.epool.pfoertner.common.retrofit;

public class MemberData {
    public final int id;
    public final String lastName;
    public final String firstName;
    public final String pictureMD5;
    public final String status;

    public MemberData(final int id, final String firstName, final String lastName, final String pictureMD5, final String status){
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.pictureMD5 = pictureMD5;
        this.status = status;
    }
}
