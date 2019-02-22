package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.InitStatusEntity;
import io.reactivex.Single;

@Dao
public interface InitStatusDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insert(InitStatusEntity initStatus);

    @Update
    void update(InitStatusEntity initStatus);

    @Query("SELECT * FROM InitStatusEntity LIMIT 1")
    Single<InitStatusEntity> get();
}
