package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import io.reactivex.Single;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class OfficeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(OfficeEntity office);

    @Update
    public abstract void update(OfficeEntity office);

    @Transaction
    public void upsert(OfficeEntity office) {
        long id = insert(office);

        if (id < 0) {
            update(office);
        }
    }

    @Query("SELECT * FROM OfficeEntity WHERE id = :officeId")
    public abstract LiveData<OfficeEntity> load(int officeId);

    @Query("SELECT * FROM OfficeEntity WHERE id = :officeId")
    public abstract Single<OfficeEntity> loadOnce(int officeId);

    @Query("SELECT * FROM OfficeEntity")
    public abstract List<OfficeEntity> getAllOffices();
}
