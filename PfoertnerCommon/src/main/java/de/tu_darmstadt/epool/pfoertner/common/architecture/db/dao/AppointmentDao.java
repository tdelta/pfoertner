package de.tu_darmstadt.epool.pfoertner.common.architecture.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.AppointmentEntity;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public abstract class AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(AppointmentEntity appointment);

    @Update
    public abstract void update(AppointmentEntity appointment);

    @Transaction
    public void upsert(AppointmentEntity appointment) {
        long id = insert(appointment);

        if (id < 0) {
            update(appointment);
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAppointments(AppointmentEntity... appointments);

    @Query("SELECT * FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract LiveData<AppointmentEntity> load(int appointmentId);

    @Query("SELECT * FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract Flowable<AppointmentEntity> loadFlowable(int appointmentId);

    @Query("SELECT * FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract Single<AppointmentEntity> loadOnce(int appointmentId);

    @Query("SELECT count(*) FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract int countEntities(int appointmentId);

    @Query("SELECT * FROM AppointmentEntity")
    public abstract List<AppointmentEntity> getAllAppointments();

    @Query("SELECT * FROM AppointmentEntity WHERE OfficeMemberId = :memberId")
    public abstract LiveData<List<AppointmentEntity>> getAllAppointmentsOfMember(int memberId);

    @Query("DELETE FROM AppointmentEntity WHERE OfficeMemberId = :memberId")
    public abstract void deleteAllAppointmentsOfMember(int memberId);

    @Query("SELECT * FROM AppointmentEntity WHERE OfficeMemberId = :memberId")
    public abstract List<AppointmentEntity> getAllAppointmentsOfMemberOnce(int memberId);
}
