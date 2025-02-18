package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.InitStatusEntity;
import io.reactivex.Single;

@Dao
public interface InitStatusDao {
    /**
     * Inserts an init status into the local database. If an init status with the same id exists, nothing happens.
     * @param initStatus The init status to insert
     */
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insert(InitStatusEntity initStatus);

    /**
     * Updates an existing init status in the local database.
     * @param initStatus The init status to update
     */
    @Update
    void update(InitStatusEntity initStatus);

    /**
     * Returns a Singe (data can be asynchronously used after loading is done) containing the first init status from the database (there should be only one init status)
     * @return A Single containing the init status object
     */
    @Query("SELECT * FROM InitStatusEntity LIMIT 1")
    Single<InitStatusEntity> get();
}
