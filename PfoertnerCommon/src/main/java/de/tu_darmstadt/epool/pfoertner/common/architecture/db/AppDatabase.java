package de.tu_darmstadt.epool.pfoertner.common.architecture.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.AppointmentDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.InitStatusDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.MemberDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.OfficeDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.AppointmentEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.DeviceDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.InitStatusDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.MemberDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao.OfficeDao;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.DeviceEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.InitStatusEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.helpers.DateConverter;

/**
 * Room automatically fills this class with useful Data access objects.
 * They are defined with queries and used as interfaces to the database
 * https://developer.android.com/topic/libraries/architecture/room
 */
@TypeConverters({DateConverter.class})
@Database(entities = {DeviceEntity.class, OfficeEntity.class, MemberEntity.class, InitStatusEntity.class, AppointmentEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
    public abstract MemberDao memberDao();
    public abstract OfficeDao officeDao();
    public abstract InitStatusDao initStatusDao();
    public abstract AppointmentDao appointmentDao();
}