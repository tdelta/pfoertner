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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(MemberEntity member);

    @Update
    public abstract void update(MemberEntity member);

    @Transaction
    public void upsert(MemberEntity member) {
        long id = insert(member);

        if (id < 0) {
            update(member);
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertMembers(MemberEntity... members);

    @Query("SELECT * FROM MemberEntity WHERE id = :memberId")
    public abstract LiveData<MemberEntity> load(int memberId);

    @Query("SELECT * FROM MemberEntity WHERE id = :memberId")
    public abstract Flowable<MemberEntity> loadFlowable(int memberId);

    @Query("SELECT * FROM MemberEntity WHERE id = :memberId")
    public abstract Single<MemberEntity> loadOnce(int memberId);

    @Query("SELECT count(*) FROM MemberEntity WHERE id = :memberId")
    public abstract int countEntities(int memberId);

    @Query("SELECT * FROM MemberEntity")
    public abstract List<MemberEntity> getAllMembers();

    @Query("SELECT * FROM MemberEntity WHERE OfficeId = :officeId")
    public abstract LiveData<List<MemberEntity>> getAllMembersFromOffice(int officeId);

    @Query("SELECT * FROM MemberEntity WHERE OfficeId = :officeId")
    public abstract List<MemberEntity> getAllMembersFromOfficeOnce(int officeId);
}
