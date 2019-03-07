package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.DeviceEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public abstract class DeviceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(DeviceEntity device);

    @Update
    public abstract void update(DeviceEntity device);

    @Transaction
    public void upsert(DeviceEntity device) {
        long id = insert(device);

        if (id < 0) {
            update(device);
        }
    }

    @Query("SELECT * FROM DeviceEntity WHERE id = :deviceId")
    public abstract LiveData<DeviceEntity> load(int deviceId);

    @Query("SELECT * FROM MemberEntity")
    public abstract List<DeviceEntity> getAllDevices();
}
