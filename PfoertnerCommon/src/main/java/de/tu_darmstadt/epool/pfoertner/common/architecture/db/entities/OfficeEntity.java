package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;

@Entity
public class OfficeEntity implements Office {
    public OfficeEntity() {}

    @Ignore
    public OfficeEntity(int id, String joinCode, String status, String room, String spionPicture, String spionPictureMD5) {
        this.id = id;
        this.joinCode = joinCode;
        this.status = status;
        this.room = room;
        this.spionPicture = spionPicture;
        this.spionPictureMD5 = spionPictureMD5;
    }

    @PrimaryKey
    private int id;

    // IMPORTANT: When updating these, dont forget to update the constructor!
    private String joinCode;
    private String status;
    private String room;
    private String spionPicture;
    private String spionPictureMD5;

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

    public void setSpionPicture(String spionPicture){ this.spionPicture = spionPicture; }

    public void setSpionPictureMD5(String spionPictureMD5){ this.spionPictureMD5 = spionPictureMD5; }

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

    @Override
    public String getSpionPicture(){ return spionPicture; }

    @Override
    public String getSpionPictureMD5() { return spionPictureMD5; }
}
