package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
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

public class AppointmentRepository {

    private static final String TAG = "AppointmentRepository";

    private final PfoertnerApi api;
    private final Authentication auth;
    private final AppDatabase db;

    private static Appointment toInterface(AppointmentEntity appointmentEntity){
        return appointmentEntity;
    }

    public AppointmentRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db){
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    public LiveData<List<Appointment>> getAppointmentsOfMember(final int memberId){
        refreshAllAppointmentsFromMember(memberId);

        return
                Transformations.map(
                    db.appointmentDao().getAllAppointmentsOfMember(memberId),
                    ArrayList<Appointment>::new
                );
    }

    public Completable removeAppointment(final int appointmentId){
        return api.removeAppointment(auth.id,appointmentId)
                .subscribeOn(Schedulers.io());
    }

    public Completable setAccepted(final int appointmentId, final boolean accepted){
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
                        prevAppointment.getOfficeMemberId()
                )
        );
    }

    private Completable modify(final int appointmentId, final Function<AppointmentEntity, AppointmentEntity> modifier) {
        return db
                .appointmentDao()
                .loadOnce(appointmentId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify appointment " + appointmentId + ", since the appointment could not be found in the database.", throwable)
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
                        throwable -> Log.e(TAG, "Could not modify appointment " + appointmentId + ", since the new data could not be uploaded.", throwable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement();
    }

    @SuppressLint("CheckResult")
    public Disposable refreshAllAppointmentsFromMember(final int memberId){
        return api
                .getAppointmentsOfMember(auth.id,memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        appointments -> {
                            Log.d(TAG,"Number of loaded appointments: "+appointments.size());
                            for(Appointment appointment : appointments){
                                Log.d(TAG,"Foreign key member: "+appointment.getOfficeMemberId());
                            }
                            db.appointmentDao().deleteAllAppointmentsOfMember(memberId);
                            db.appointmentDao().insertAppointments(appointments.toArray(new AppointmentEntity[0]));
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        appointmentEntities -> {},
                        throwable -> Log.e(TAG,"Could not refresh appointments of member "+memberId)
                );
    }
}