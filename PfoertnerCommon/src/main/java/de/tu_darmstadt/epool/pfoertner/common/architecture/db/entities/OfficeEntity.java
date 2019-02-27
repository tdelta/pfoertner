package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;

@Entity
public class OfficeEntity implements Office {
    public OfficeEntity() {}

    @Ignore
    public OfficeEntity(int id, String joinCode, String status, String room) {
        this.id = id;
        this.joinCode = joinCode;
        this.status = status;
        this.room = room;
    }

    @PrimaryKey
    private int id;

    private String joinCode;
    private String status;
    private String room;

    public void setId(int id) {
        this.id = id;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRoom(String room) { this.room = room; }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getRoom() { return room; }

    @Override
    public String getJoinCode() {
        return joinCode;
    }
}
