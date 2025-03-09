package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.TimeslotEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Timeslot;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides access to {@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Timeslot}
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
public class TimeslotRepository {
    private static final String TAG = "TimeslotRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    /**
     * Internal helper function, which helps to cast the Timeslot database entity
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.TimeslotEntity}) to the
     * corresponding model interface it implements
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Device}).
     * <p>
     * This way, clients wont have access to database specific functionality, since the
     * Repository Pattern wants to abstract from it.
     *
     * @param entity to be cast to model interface
     * @return same entity, but as model interface type
     */
    private static Timeslot toInterface(final TimeslotEntity entity) {
        return entity; //  implicit cast to interface
    }

    /**
     * Creates a repository instance.
     *
     * @param api  used for network calls if information is not available locally or needs to
     *             refreshed
     * @param auth authentication data necessary to use network api
     * @param db   database where data is being cached
     */
    public TimeslotRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    /**
     * Retrieves Timeslot data by the id of the device. Since it returns {@link LiveData}, the
     * retrieved data is always refreshed, as soon as the value in the repository changes.
     * <p>
     * Calling this method will initiate an asynchronous network refresh, but until that is
     * finished, the LiveData will contain the old cached data, or null.
     *
     * @param until Cutoff date, events with later start will be excluded
     * @param memberId id of the office member that the time slots shall be associated with.
     * @return auto-refreshing device data
     */
    public LiveData<List<Timeslot>> getTimeslotsOfMember(LocalDateTime until, final int memberId) {
        refreshAllTimeslotsFromMember(until, memberId);

        return
                Transformations.map(
                        db.timeslotDao().getAllTimeslotsOfMember(memberId),
                        ArrayList<Timeslot>::new
                );
    }

    /**
     * Will asynchronously refresh the locally available time slot data for all time slots with
     * the given member id.
     *
     * Attention: This function might temporarily purge all cached appointments from the
     * database, before inserting the new ones.
     *
     * @param until Cutoff date, events with later start will be excluded
     * @param memberId id of the appointment, whose data shall be retrieved
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshAllTimeslotsFromMember(final LocalDateTime until, final int memberId) {
        return api
                .getTimeslotsOfMember(auth.id, memberId, until)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        timeslots -> {
                            db.timeslotDao().deleteAllTimeslotsOfMember(memberId);
                            db.timeslotDao().insertTimeslots(timeslots.toArray(new TimeslotEntity[0]));
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        appointmentEntities -> {
                        },
                        throwable -> Log.e(TAG, "Could not refresh time slots of member " + memberId, throwable)
                );
    }
}