package de.tu_darmstadt.epool.pfoertneradmin.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import de.tu_darmstadt.epool.pfoertneradmin.db.entity.OfficeEntity;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface OfficeDao {
    @Insert(onConflict = REPLACE)
    void save(OfficeEntity office);

    @Query("SELECT * FROM officeEntity WHERE id = :officeId")
    LiveData<OfficeEntity> load(int officeId);

    @Query("SELECT count(*) FROM officeEntity WHERE id = :officeId")
    int countEntities(int officeId);
}
