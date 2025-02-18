package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.AppointmentEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides access to {@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment}
 * data and methods to modify that data.
 * <p>
 * It loosely implements the Repository Pattern (@see
 * <a href="http://msdn.microsoft.com/en-us/library/ff649690.aspx">link</a>
 * ) and therefore abstracts from the data sources
 * (database / network calls to the server).
 * <p>
 * Through this class, the database is enforced as single source of truth, all data accessible
 * through this repository, even when obtained from the network, has been written to the database
 * first.
 * This shall minimize data inconsistencies within the application.
 */
public class AppointmentRepository {

    private static final String TAG = "AppointmentRepository";

    private final PfoertnerApi api;
    private final Authentication auth;
    private final AppDatabase db;

    /**
     * Internal helper function, which helps to cast the Appointment database entity
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.AppointmentEntity}
     * ) to the
     * corresponding model interface it implements
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment}).
     * <p>
     * This way, clients wont have access to database specific functionality, since the
     * Repository Pattern wants to abstract from it.
     *
     * @param appointmentEntity to be cast to model interface
     * @return same entity, but as model interface type
     */
    private static Appointment toInterface(AppointmentEntity appointmentEntity) {
        return appointmentEntity;
    }

    /**
     * Creates a repository instance.
     *
     * @param api  used for network calls if information is not available locally or needs to
     *             refreshed
     * @param auth authentication data necessary to use network api
     * @param db   database where data is being cached
     */
    public AppointmentRepository(final PfoertnerApi api, final Authentication auth,
                                 final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    /**
     * Retrieves Appointment data by the id of the member for which an appointment had been
     * requested. Since it returns {@link LiveData}, the retrieved data is always refreshed, as
     * soon as the value in the repository changes.
     * <p>
     * Calling this method will initiate an asynchronous network refresh, but until that is
     * finished, the LiveData will contain the old cached data, or null.
     *
     * @param memberId id of the member for which the data model shall be retrieved.
     * @return auto-refreshing appointment data
     */
    public LiveData<List<Appointment>> getAppointmentsOfMember(final int memberId) {
        refreshAllAppointmentsFromMember(memberId);

        return
                Transformations.map(
                        db.appointmentDao().getAllAppointmentsOfMember(memberId),
                        ArrayList<Appointment>::new
                );
    }

    /**
     * Retrieves Appointment data by the athene card id that was registered when requesting the appointment. Since it returns {@link LiveData}, the retrieved data is always refreshed, as
     * soon as the value in the repository changes.
     * <p>
 *     Calling this method will <b>not</b> initiate an asynchronous network request
     * @param atheneId id of the athene card of which the appointments shall be retrieved.
     * @return auto-refreshing appointment data
     */
    public LiveData<List<Appointment>> getAppointmentsForAtheneId(final String atheneId){
        // TODO: Refresh appointments for office (useless if there is only one panel)
        return Transformations.map(
                db.appointmentDao().getAppointmentsByAtheneId(atheneId),
                ArrayList<Appointment>::new
        );
    }

    /**
     * Instructs the server to decline an appointment.
     *
     * @param appointmentId id of the appointment to decline
     * @return Completable that will complete, as soon as the server received the instruction.
     */
    public Completable removeAppointment(final int appointmentId) {
        return api
                .removeAppointment(auth.id, appointmentId)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Sets an appointment's status to accepted or not accepted. The change will be propagated
     * to the server and all other devices, which use the appointment.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param appointmentId  id of the appointment, which shall (not) be accepted
     * @param accepted indicates, whether the appointment shall be set to be accepted or not
     *                 accepted
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setAccepted(final int appointmentId, final boolean accepted) {
        return modify(
                appointmentId,
                prevAppointment -> new AppointmentEntity(
                        prevAppointment.getId(),
                        prevAppointment.getStart(),
                        prevAppointment.getEnd(),
                        prevAppointment.getEmail(),
                        prevAppointment.getName(),
                        prevAppointment.getMessage(),
                        accepted,
                        prevAppointment.getOfficeMemberId(),
                        prevAppointment.getAtheneId()
                )
        );
    }

    public Completable createAppointment(final AppointmentEntity appointment){
        return api
                .createNewAppointment(auth.id,appointment.getOfficeMemberId(),appointment)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Internal helper method, which is used to update appointment data on the server. It is usually
     * employed by setter methods.
     * <p>
     * It will use the locally cached appointment as base for the update.
     *
     * @param appointmentId id of appointment of which data shall be changed
     * @param modifier a function, should return a new AppointmentEntity object with the applied
     *                 changes. It will be given the current state of the appointment as input.
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    private Completable modify(final int appointmentId, final Function<AppointmentEntity,
            AppointmentEntity> modifier) {
        return db
                .appointmentDao()
                .loadOnce(appointmentId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG,
                                "Could not modify appointment " + appointmentId + ", since the " +
                                        "appointment could not be found in the database.",
                                throwable)
                )
                .flatMap(
                        appointmentEntity -> api
                                .patchAppointment(
                                        auth.id,
                                        appointmentEntity.getId(),
                                        modifier.apply(appointmentEntity)
                                )
                )
                .doOnError(
                        throwable -> Log.e(TAG,
                                "Could not modify appointment " + appointmentId + ", since the " +
                                        "new data could not be uploaded.", throwable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement();
    }

    /**
     * Will asynchronously refresh the locally available appointment data for all appointments with
     * the given member id.
     *
     * Attention: This function might temporarily purge all cached appointments from the
     * database, before inserting the new ones.
     *
     * @param memberId id of the appointment, whose data shall be retrieved
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshAllAppointmentsFromMember(final int memberId) {
        return api
                .getAppointmentsOfMember(auth.id, memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        appointments -> {
                            db.appointmentDao().deleteAllAppointmentsOfMember(memberId);
                            db.appointmentDao().insertAppointments(appointments.toArray(new AppointmentEntity[0]));
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        appointmentEntities -> {
                        },
                        throwable -> Log.e(TAG, "Could not refresh appointments of member " + memberId)
                );
    }
}