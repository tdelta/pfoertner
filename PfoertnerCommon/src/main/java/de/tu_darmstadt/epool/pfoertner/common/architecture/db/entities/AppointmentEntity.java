package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

import de.tu_darmstadt.epool.pfoertner.common.architecture.helpers.DateConverter;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;

@Entity(foreignKeys = @ForeignKey(entity = MemberEntity.class,
        parentColumns = "id",
        childColumns = "OfficeMemberId",
        onDelete = ForeignKey.CASCADE)
)
public class AppointmentEntity implements Appointment{

    public AppointmentEntity() {}

    @Ignore
    public AppointmentEntity(
            int id,
            Date start,
            Date end,
            String email,
            String name,
            String message,
            boolean accepted,
            int OfficeMemberId
    ) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.email = email;
        this.name = name;
        this.message = message;
        this.accepted = accepted;
        this.OfficeMemberId = OfficeMemberId;
    }

    @PrimaryKey
    private int id;
    private Date start;
    private Date end;
    private String email;
    private String name;
    private String message;
    private boolean accepted;
    private int OfficeMemberId;

    public void setOfficeMemberId(int memberId) {
        this.OfficeMemberId = memberId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Date getStart() {
        return start;
    }

    @Override
    public Date getEnd() {
        return end;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean getAccepted() {
        return accepted;
    }

    @Override
    public int getOfficeMemberId() {
        return OfficeMemberId;
    }
}
