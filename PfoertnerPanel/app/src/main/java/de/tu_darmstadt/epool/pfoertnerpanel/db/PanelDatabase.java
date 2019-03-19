package de.tu_darmstadt.epool.pfoertnerpanel.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;

import de.tu_darmstadt.epool.pfoertner.common.architecture.helpers.DateConverter;
import de.tu_darmstadt.epool.pfoertnerpanel.db.dao.MemberCalendarInfoDao;
import de.tu_darmstadt.epool.pfoertnerpanel.db.entities.MemberCalendarInfoEntity;

/**
 * Provides direct access to the underlying database implementation.
 */
@TypeConverters({DateConverter.class})
@Database(entities = {MemberCalendarInfoEntity.class}, version = 1)
public abstract class PanelDatabase extends RoomDatabase {
    public abstract MemberCalendarInfoDao memberCalendarInfoDao();
}
