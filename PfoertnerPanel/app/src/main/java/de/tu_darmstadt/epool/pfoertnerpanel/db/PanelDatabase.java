package de.tu_darmstadt.epool.pfoertnerpanel.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import de.tu_darmstadt.epool.pfoertnerpanel.db.dao.MemberCalendarInfoDao;
import de.tu_darmstadt.epool.pfoertnerpanel.db.entities.MemberCalendarInfoEntity;

@Database(entities = {MemberCalendarInfoEntity.class}, version = 1)
public abstract class PanelDatabase extends RoomDatabase {
    public abstract MemberCalendarInfoDao memberCalendarInfoDao();
}
