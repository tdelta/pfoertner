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
    /**
     * Inserts an appointment into the local database, does nothing, if an appointment with the same id exists
     * @param appointment The appointment to insert
     * @return The id of the new appointment, or a value < 0 if an error occured
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(AppointmentEntity appointment);

    /**
     * Updates an existing appointment in the local database
     * @param appointment The appointment to update
     */
    @Update
    public abstract void update(AppointmentEntity appointment);

    /**
     * Updates an existing appointment or inserts a new one, if it doesnt exist
     * @param appointment The appointment to update or insert
     */
    @Transaction
    public void upsert(AppointmentEntity appointment) {
        long id = insert(appointment);

        if (id < 0) {
            // Appointment exists in the database
            update(appointment);
        }
    }

    /**
     * Inserts a list of appointments into the local database, replaces them if appointments with the same ids already exist.
     * @param appointments The appointments to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAppointments(AppointmentEntity... appointments);

    /**
     * Loads a LiveData Object (changes in the database can be observed by an activity) for an appointment
     * @param appointmentId The id of the appointment to load
     * @return LiveData cointaining the requested appointment
     */
    @Query("SELECT * FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract LiveData<AppointmentEntity> load(int appointmentId);

    /**
     * Loads a Flowable Object (changes in the database can be observed by an rxjava observer) for an appointment
     * @param appointmentId The id of the appointment to load
     * @return Flowable containing the requested appointment
     */
    @Query("SELECT * FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract Flowable<AppointmentEntity> loadFlowable(int appointmentId);

    /**
     * Loads a Single Object (data can be asynchronously used after loading is done) for an appointment
     * @param appointmentId The id of the appointment to load
     * @return Single containing the requested appointment
     */
    @Query("SELECT * FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract Single<AppointmentEntity> loadOnce(int appointmentId);

    /**
     * Counts appointments in the local database with a specific id
     * @param appointmentId Id of the appointments
     * @return The number of appointments with the given id
     */
    @Query("SELECT count(*) FROM AppointmentEntity WHERE id = :appointmentId")
    public abstract int countEntities(int appointmentId);

    /**
     * Loads a Single Object (data can be asynchronously used after loading is done) for all appointments in the database.
     * @return Single Object containing a list of appointments
     */
    @Query("SELECT * FROM AppointmentEntity")
    public abstract Single<List<AppointmentEntity>> getAllAppointments();

    /**
     * Loads a LiveData Object (can be observed by an activity) containing all appointments in the local database,
     * that belong to a specified office member
     * @param memberId The id of the office member (foreign key in appointment)
     * @return LiveData containing the requested appointments
     */
    @Query("SELECT * FROM AppointmentEntity WHERE OfficeMemberId = :memberId")
    public abstract LiveData<List<AppointmentEntity>> getAllAppointmentsOfMember(int memberId);

    /**
     * Deletes all appointments from the local database that belong to a specific office member
     * @param memberId The id of the office member
     */
    @Query("DELETE FROM AppointmentEntity WHERE OfficeMemberId = :memberId")
    public abstract void deleteAllAppointmentsOfMember(int memberId);

    /**
     * Loads a LiveData Object (can be observed by an activity) containing all appointments with a specific athene id.
     * @param atheneId The id of the athene card of the person who requested the appointment.
     * @return LiveData containing the requested appointments.
     */
    @Query("SELECT * FROM AppointmentEntity WHERE atheneId = :atheneId")
    public abstract LiveData<List<AppointmentEntity>> getAppointmentsByAtheneId(String atheneId);
}
