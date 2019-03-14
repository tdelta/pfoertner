package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import io.reactivex.Flowable;
import io.reactivex.Single;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class MemberDao {
    /**
     * Inserts an office member into the local database, does nothing if an office member with the same id exists
     * @param member The member to insert
     * @return The id of the new member, or a value < 0 if an error occured
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(MemberEntity member);

    /**
     * Updates an existing office member in the local database
     * @param member The office member to update
     */
    @Update
    public abstract void update(MemberEntity member);

    /**
     * Updates an existing office member or inserts a new one, if it doesnt exist
     * @param member The office member to update or insert
     */
    @Transaction
    public void upsert(MemberEntity member) {
        long id = insert(member);

        if (id < 0) {
            // Office member already exists
            update(member);
        }
    }

    /**
     * Inserts a list of office members into the local database, skips office members if one with the same id already exist.
     * @param members The office members to insert
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertMembers(MemberEntity... members);

    /**
     * Loads a LiveData Object (changes in the database can be observed by an activity) for an office member
     * @param memberId The id of the office member to load
     * @return LiveData cointaining the requested office member
     */
    @Query("SELECT * FROM MemberEntity WHERE id = :memberId")
    public abstract LiveData<MemberEntity> load(int memberId);

    /**
     * Loads a Single Object (data can be asynchronously used after loading is done) for an office member
     * @param memberId The id of the office member to load
     * @return Single containing the requested office member
     */
    @Query("SELECT * FROM MemberEntity WHERE id = :memberId")
    public abstract Single<MemberEntity> loadOnce(int memberId);

    /**
     * Counts office members in the local database with a specific id
     * @param memberId Id of the office members
     * @return The number of office members with the given id
     */
    @Query("SELECT count(*) FROM MemberEntity WHERE id = :memberId")
    public abstract int countEntities(int memberId);

    /**
     * Loads all office members from the local database in a blocking call
     * @return The office members
     */
    @Query("SELECT * FROM MemberEntity")
    public abstract List<MemberEntity> getAllMembers();

    /**
     * Loads a LiveData Object (can be observed by an activity) containing all office members in the local database,
     * that belong to a specified office
     * @param officeId The id of the office (foreign key in office member)
     * @return LiveData containing the requested appointments
     */
    @Query("SELECT * FROM MemberEntity WHERE OfficeId = :officeId")
    public abstract LiveData<List<MemberEntity>> getAllMembersFromOffice(int officeId);

    /**
     * Load all office members that belong to a given office in a blocking call
     * @param officeId The id of the office
     * @return A List of office members
     */
    @Query("SELECT * FROM MemberEntity WHERE OfficeId = :officeId")
    public abstract List<MemberEntity> getAllMembersFromOfficeOnce(int officeId);
}
