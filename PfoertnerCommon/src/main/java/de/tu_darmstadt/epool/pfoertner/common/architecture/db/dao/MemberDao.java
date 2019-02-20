package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface MemberDao {
    @Insert(onConflict = REPLACE)
    void save(MemberEntity member);

    @Query("SELECT * FROM memberEntity WHERE id = :memberId")
    LiveData<MemberEntity> load(int memberId);

    @Query("SELECT count(*) FROM memberEntity WHERE id = :memberId")
    int countEntities(int memberId);
}
