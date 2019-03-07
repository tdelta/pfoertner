package de.tu_darmstadt.epool.pfoertner.common.architecture.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.DeviceDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.InitStatusDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.MemberDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.OfficeDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.DeviceEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.InitStatusEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;

@Database(entities = {DeviceEntity.class, OfficeEntity.class, MemberEntity.class, InitStatusEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
    public abstract MemberDao memberDao();
    public abstract OfficeDao officeDao();
    public abstract InitStatusDao initStatusDao();
}
