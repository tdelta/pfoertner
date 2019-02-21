package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;

@Entity(foreignKeys = @ForeignKey(entity = OfficeEntity.class,
        parentColumns = "id",
        childColumns = "OfficeId",
        onDelete = ForeignKey.CASCADE)
)
public class MemberEntity implements Member {
    public MemberEntity() {}

    @Ignore
    public MemberEntity(int id, int officeId, String firstName, String lastName, String status) {
        this.id = id;
        this.OfficeId = officeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
    }

    @PrimaryKey
    private int id;

    private int OfficeId;

    private String firstName;
    private String lastName;
    private String status;

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
}
