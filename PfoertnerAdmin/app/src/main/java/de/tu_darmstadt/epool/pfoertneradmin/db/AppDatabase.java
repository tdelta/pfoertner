package de.tu_darmstadt.epool.pfoertneradmin.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import dagger.Module;
import dagger.Provides;
import de.tu_darmstadt.epool.pfoertneradmin.db.dao.MemberDao;
import de.tu_darmstadt.epool.pfoertneradmin.db.dao.OfficeDao;
import de.tu_darmstadt.epool.pfoertneradmin.db.entity.MemberEntity;
import de.tu_darmstadt.epool.pfoertneradmin.db.entity.OfficeEntity;

@Database(entities = {OfficeEntity.class, MemberEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MemberDao memberDao();

    public abstract OfficeDao officeDao();
}
