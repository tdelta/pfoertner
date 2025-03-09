package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;

@Entity(foreignKeys = @ForeignKey(
            entity = OfficeEntity.class,
            parentColumns = "id",
            childColumns = "OfficeId",
            onDelete = ForeignKey.CASCADE
    )
)
public class MemberEntity implements Member {
    public MemberEntity() {}

    @Ignore
    public MemberEntity(int id, int officeId, String firstName, String lastName, String status, String picture, String pictureMD5, String email) {
        this.id = id;
        this.OfficeId = officeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.picture = picture;
        this.pictureMD5 = pictureMD5;
        this.email = email;
    }

    @PrimaryKey
    private int id;

    private int OfficeId;

    // IMPORTANT: When updating these, dont forget to update the constructor!
    private String firstName;
    private String lastName;
    private String status;
    private String picture;
    private String pictureMD5;
    private String email;

    public void setId(int id) {
        this.id = id;
    }

    public void setOfficeId(int officeId) {
        OfficeId = officeId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPicture(String path) { this.picture = path; }

    public void setPictureMD5(String hash) { this.pictureMD5 = hash; }

    public void setEmail(String email) { this.email = email; }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getOfficeId() {
        return OfficeId;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getPicture() { return picture; }

    @Override
    public String getPictureMD5() { return pictureMD5; }

    @Override
    public String getEmail() { return email; }

    /**
     * Creates a MemberEntity Object with the same values as the current object
     * @return A new MemberEntity Object
     */
    public MemberEntity deepCopy() {
        return new MemberEntity(
                this.getId(),
                this.getOfficeId(),
                this.getFirstName(),
                this.getLastName(),
                this.getStatus(),
                this.getPicture(),
                this.getPictureMD5(),
                this.getEmail()
        );
    }
}
