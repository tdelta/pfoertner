package de.tu_darmstadt.epool.pfoertner.common.architecture.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.MemberDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.OfficeDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;

@Database(entities = {OfficeEntity.class, MemberEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MemberDao memberDao();

    public abstract OfficeDao officeDao();
}
