package de.tu_darmstadt.epool.pfoertnerpanel.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

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
