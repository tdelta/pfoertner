package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.DeviceEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public abstract class DeviceDao {
    /**
     * Inserts a device into the local database, does nothing if a device with the same id already exists
     * @param device The device to insert
     * @return The id of the new device, or a value < 0 if an error occured
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(DeviceEntity device);

    /**
     * Updates an existing device in the database
     * @param device The device to update
     */
    @Update
    public abstract void update(DeviceEntity device);

    /**
     * Updates an existing device or inserts a new one, if it doesnt exist
     * @param device The device to update or insert
     */
    @Transaction
    public void upsert(DeviceEntity device) {
        long id = insert(device);

        if (id < 0) {
            // The device already exists
            update(device);
        }
    }

    /**
     * Loads a LiveData Object (changes in the database can be observed by an activity) for a device
     * @param deviceId The id of the device to load
     * @return LiveData containing the device
     */
    @Query("SELECT * FROM DeviceEntity WHERE id = :deviceId")
    public abstract LiveData<DeviceEntity> load(int deviceId);

    /**
     * Loads all devices from the local database in a blocking call
     * @return List of all device in the local database
     */
    @Query("SELECT * FROM DeviceEntity")
    public abstract List<DeviceEntity> getAllDevices();
}
