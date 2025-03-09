package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;
import java.util.Date;

import de.tu_darmstadt.epool.pfoertner.common.architecture.helpers.DateConverter;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Timeslot;

@Entity(foreignKeys = @ForeignKey(entity = MemberEntity.class,
        parentColumns = "id",
        childColumns = "OfficeMemberId",
        onDelete = ForeignKey.CASCADE)
)
public class TimeslotEntity implements Timeslot {

    @NonNull
    @PrimaryKey
    private String id;

    private int OfficeMemberId;
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeslotEntity(@NonNull String id) {
        this.id = id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setOfficeMemberId(int officeMemberId) {
        this.OfficeMemberId = officeMemberId;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getOfficeMemberId() {
        return OfficeMemberId;
    }

    @Override
    public LocalDateTime getStart() {
        return start;
    }

    @Override
    public LocalDateTime getEnd() {
        return end;
    }
}
