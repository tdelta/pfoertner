package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.TimeslotEntity;

@Dao
public abstract class TimeslotDao {

    /**
     * Inserts a list of time slots into the local database, replaces them if time slots with the same ids already exist.
     * @param timeslots The time slots to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTimeslots(TimeslotEntity... timeslots);

    /**
     * Loads a LiveData Object (can be observed by an activity) containing all time slots in the local database,
     * that belong to a specified office member
     * @param memberId The id of the office member (foreign key in time slot)
     * @return LiveData containing the requested timeslots
     */
    @Query("SELECT * FROM TimeslotEntity WHERE OfficeMemberId = :memberId")
    public abstract LiveData<List<TimeslotEntity>> getAllTimeslotsOfMember(int memberId);

    /**
     * Deletes all time slots from the local database that belong to a specific office member
     * @param memberId The id of the office member
     */
    @Query("DELETE FROM TimeslotEntity WHERE OfficeMemberId = :memberId")
    public abstract void deleteAllTimeslotsOfMember(int memberId);
}
