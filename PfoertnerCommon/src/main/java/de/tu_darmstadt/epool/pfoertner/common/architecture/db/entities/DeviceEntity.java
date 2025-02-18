package de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Device;

@Entity
public class DeviceEntity implements Device {
    public DeviceEntity() {}

    @Ignore
    public DeviceEntity(int id, String fcmToken) {
        this.id = id;
        this.fcmToken = fcmToken;
    }

    @PrimaryKey
    private int id;

    // IMPORTANT: When updating these, dont forget to update the constructor!
    private String fcmToken;

    public void setId(int id) {
        this.id = id;
    }

    public void setFcmToken(final String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getFcmToken() {
        return this.fcmToken;
    }

    /**
     * Creates DeviceEntity Object with the same values as the current object
     * @return A new DeviceEntity
     */
    public DeviceEntity deepCopy() {
        return new DeviceEntity(
                this.getId(),
                this.fcmToken
        );
    }
}
