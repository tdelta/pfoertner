package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.InitStatus;

@Entity
public class InitStatusEntity implements InitStatus {
    @PrimaryKey
    private int id;

    private int joinedOfficeId;

    public InitStatusEntity() {
        this.id = 0;
        this.joinedOfficeId = -1;
    }

    @Ignore
    public InitStatusEntity(int id, int joinedOfficeId) {
        this.id = id;
        this.joinedOfficeId = joinedOfficeId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setJoinedOfficeId(int joinedOfficeId) {
        this.joinedOfficeId = joinedOfficeId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int joinedOfficeId() {
        return joinedOfficeId;
    }
}
