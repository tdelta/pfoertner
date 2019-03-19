package de.tu_darmstadt.epool.pfoertnerpanel.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import de.tu_darmstadt.epool.pfoertnerpanel.db.entities.MemberCalendarInfoEntity;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Data Access Objects class defines database interactions. 
 * It includes a variety of query methods. 
 */
@Dao
public abstract class MemberCalendarInfoDao {
    @Query("SELECT * FROM MemberCalendarInfoEntity WHERE memberId = :memberId")
    public abstract LiveData<MemberCalendarInfoEntity> load(int memberId);

    @Query("SELECT * FROM MemberCalendarInfoEntity WHERE memberId = :memberId")
    public abstract Single<MemberCalendarInfoEntity> loadOnce(int memberId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(MemberCalendarInfoEntity calendarInfo);

    @Update
    public abstract void update(MemberCalendarInfoEntity calendarInfo);

    @Transaction
    public void upsert(MemberCalendarInfoEntity calendarInfo) {
        long id = insert(calendarInfo);

        if (id < 0) {
            update(calendarInfo);
        }
    }
}
