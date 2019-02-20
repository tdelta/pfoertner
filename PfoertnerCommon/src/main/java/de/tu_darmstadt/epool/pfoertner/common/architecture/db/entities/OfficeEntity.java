package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;

@Entity
public class OfficeEntity implements Office {
    public OfficeEntity() {}

    public OfficeEntity(int id, String joinCode, String status) {
        this.id = id;
        this.joinCode = joinCode;
        this.status = status;
    }

    @PrimaryKey
    private int id;

    private String joinCode;
    private String status;

    public void setId(int id) {
        this.id = id;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getJoinCode() {
        return joinCode;
    }
}
