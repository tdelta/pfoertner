package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import io.reactivex.Single;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class OfficeDao {
    /**
     * Inserts an office into the local database, does nothing, if an office with the same id exists
     * @param office The office to insert
     * @return The id of the new office, or a value < 0 if an error occured
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(OfficeEntity office);

    /**
     * Updates an existing office in the local database
     * @param office The office to update
     */
    @Update
    public abstract void update(OfficeEntity office);

    /**
     * Updates an existing office or inserts a new one, if it doesnt exist
     * @param office The office to update or insert
     */
    @Transaction
    public void upsert(OfficeEntity office) {
        long id = insert(office);

        if (id < 0) {
            // The office already exists
            update(office);
        }
    }

    /**
     * Loads a LiveData Object (changes in the database can be observed by an activity) for an office
     * @param officeId The id of the office to load
     * @return LiveData cointaining the requested office
     */
    @Query("SELECT * FROM OfficeEntity WHERE id = :officeId")
    public abstract LiveData<OfficeEntity> load(int officeId);

    /**
     * Loads a Flowable Object (changes in the database can be observed by an rxjava observer) for an office
     * @param officeId The id of the office to load
     * @return Flowable containing the requested office
     */
    @Query("SELECT * FROM OfficeEntity WHERE id = :officeId")
    public abstract Single<OfficeEntity> loadOnce(int officeId);

    /**
     * Loads all offices from the local database in a blocking call
     * @return A List of all offices
     */
    @Query("SELECT * FROM OfficeEntity")
    public abstract List<OfficeEntity> getAllOffices();
}
