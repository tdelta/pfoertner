package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Provides access to {@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member}
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
public class MemberRepository {
    private static final String TAG = "MemberRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    /**
     * Internal helper function, which helps to cast the Member database entity
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity}) to the
     * corresponding model interface it implements
     * ({@link de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member}).
     * <p>
     * This way, clients wont have access to database specific functionality, since the
     * Repository Pattern wants to abstract from it.
     *
     * @param entity to be cast to model interface
     * @return same entity, but as model interface type
     */
    private static Member toInterface(final MemberEntity entity) {
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
    public MemberRepository(final PfoertnerApi api, final Authentication auth,
                            final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    /**
     * Retrieves Member data by the id of the member. Since it returns {@link LiveData}, the
     * retrieved data is always refreshed, as soon as the value in the repository changes.
     * <p>
     * Calling this method will initiate an asynchronous network refresh, but until that is
     * finished, the LiveData will contain the old cached data, or null.
     *
     * @param memberId id of the member for which the data model shall be retrieved.
     * @return auto-refreshing member data
     */
    public LiveData<Member> getMember(final int memberId) {
        refreshMember(memberId);

        return Transformations.map(
                db.memberDao().load(memberId),
                MemberRepository::toInterface
        );
    }

    /**
     * Same as {@link #getMember(int)}, but will retrieve all members which belong to a specific
     * office instead of a single one.
     * <p>
     * The returned {@link LiveData} will refresh, when any member contained in the list is
     * refreshed.
     *
     * @param officeId id of the office for which we want to retrieve all members
     * @return auto-refreshing list of members
     */
    public LiveData<List<Member>> getMembersFromOffice(final int officeId) {
        refreshAllMembersFromOffice(officeId);

        return
                Transformations.map(
                        db.memberDao().getAllMembersFromOffice(officeId),
                        ArrayList<Member>::new // implicit conversion to pure interface type
                );
    }

    /**
     * Same as {@link #getMember}, but it will provide only the currently available member data
     * and wont auto-refresh.
     * <p>
     * Attention: It will *not* load data from the server, if there is none cached.
     *
     * @param memberId id of the member for which the data model shall be retrieved.
     * @return member data for the given id
     */
    public Single<Member> getMemberOnce(final int memberId) {
        return db.memberDao()
                .loadOnce(memberId)
                .map(MemberRepository::toInterface);
    }

    /**
     * Sets a new status string for the member with the given data. The change will be propagated
     * to the server and all other members, which use the member.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param memberId  id of the member, for which the status shall be changed
     * @param newStatus new status to be set for the member
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setStatus(final int memberId, final String newStatus) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setStatus(newStatus)
        );
    }

    /**
     * Sets a new first name for the member with the given data. The change will be
     * propagated
     * to the server and all other members, which use the member.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param memberId  id of the member, for which the status shall be changed
     * @param firstName new first name to be set for the member
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setFirstName(final int memberId, final String firstName) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setFirstName(firstName)
        );
    }

    /**
     * Sets a new last name for the member with the given data. The change will be
     * propagated
     * to the server and all other members, which use the member.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param memberId id of the member, for which the status shall be changed
     * @param lastName new last name to be set for the member
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setLastName(final int memberId, final String lastName) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setLastName(lastName)
        );
    }

    /**
     * Sets a new calendar server authentication code for the member with the given data. The
     * change will be propagated to the server and all other members, which use the member.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param memberId       id of the member, for which the status shall be changed
     * @param serverAuthCode new google calendar server auth code needed for accessing the
     *                       calendar of the member
     * @param eMail          mail associated with the calendar
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setServerAuthCode(final int memberId, final String serverAuthCode,
                                         final String eMail) {
        return modify(
                memberId,
                prevMemberClone ->
                {
                    prevMemberClone.setServerAuthCode(serverAuthCode);
                    prevMemberClone.setEmail(eMail);
                }
        );
        // TODO: Dont synchronize EMail
    }

    /**
     * Sets id of the connected google calendar for the member with the given data. The change
     * will be propagated to the server and all other members, which use the member.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param memberId  id of the member, for which the status shall be changed
     * @param calendarId id of the connected google calendar of the member
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setCalendarId(int memberId, String calendarId) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setCalendarId(calendarId)
        );
    }


    /**
     * Sets id of the google calendar webhook of the member with the given member id. The change
     * will be propagated to the server and all other members, which use the member.
     * <p>
     * This method does not directly change the locally cached data. The local data will only be
     * refreshed, as soon as the server instructs us to do so
     * (see {@link de.tu_darmstadt.epool.pfoertner.common.SyncService}.
     *
     * @param memberId  id of the member, for which the status shall be changed
     * @param webhookId id of the webhook of the connected google calendar of the member
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    public Completable setWebhookId(int memberId, String webhookId) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setWebhookId(webhookId)
        );
    }

    /**
     * Internal helper method, which is used to update member data on the server. It is usually
     * employed by setter methods.
     * <p>
     * It will use the locally cached member as base for the update.
     *
     * @param memberId id of member of which data shall be changed
     * @param modifier a function, which applies changes to the current state of the member data
     * @return a Completable, which indicates, whether the server has been informed about the
     * change yet.
     */
    private Completable modify(final int memberId, final Consumer<MemberEntity> modifier) {
        return db
                .memberDao()
                .loadOnce(memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify member " + memberId + ", since " +
                                "the member could not be found in the database.", throwable)
                )
                .flatMap(
                        member -> {
                            final MemberEntity replacement = member.deepCopy();

                            modifier.accept(replacement);

                            return api
                                    .patchMember(
                                            auth.id,
                                            member.getId(),
                                            replacement
                                    );
                        }
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify member " + memberId + ", since " +
                                "the new data could not be uploaded.", throwable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement();
    }

    /**
     * Will asynchronously refresh the locally available member data for the member with the
     * given id.
     *
     * @param memberId id of the member, whose data shall be retrieved
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshMember(final int memberId) {
        Log.d(TAG, "About to refresh member...");

        return api
                .getMember(auth.id, memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        memberEntity -> {
                            Log.d(
                                    TAG,
                                    "Successfully retrieved new information about member " +
                                            memberId +
                                            ". This is the new info: " +
                                            "(id: " +
                                            memberEntity.getId() +
                                            ", office id: " +
                                            memberEntity.getOfficeId() +
                                            ", Name: " +
                                            memberEntity.getFirstName() +
                                            " " +
                                            memberEntity.getLastName() +
                                            ") Will now insert it into the db."
                            );

                            db.memberDao().upsert(memberEntity);
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        memberEntity -> {
                        },
                        throwable -> Log.e(TAG, "Could not refresh member with id " + memberId,
                                throwable)
                );
    }

    /**
     * Same as {@link #refreshMember}, but will refresh the data of all members, which are
     * currently cached locally.
     *
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshAllLocalData() {
        Log.d(TAG, "About to refresh all data of already known members...");

        return Single
                .fromCallable(
                        db.memberDao()::getAllMembers
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        members -> members.forEach(
                                memberEntity -> refreshMember(memberEntity.getId())
                        ),
                        throwable -> Log.e(TAG, "Could not refresh all members.", throwable)
                );
    }

    /**
     * Same as {@link #refreshMember}, but will refresh the data of all members, which are
     * member of the office with the given id.
     *
     * @param officeId id of the office, for which we want to refresh the data of all members
     * @return handle which can be used to dispose of the asynchronous observer which is used
     * internally
     */
    public Disposable refreshAllMembersFromOffice(final int officeId) {
        Log.d(TAG, "About to refresh all data for members of office " + officeId + "...");

        return api
                .getMembersFromOffice(auth.id, officeId)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(
                        memberEntities -> {
                            Log.d(
                                    TAG,
                                    "Successfully retrieved new information about all members of " +
                                            "office " +
                                            officeId +
                                            ". Inserting the following info into db:"
                            );

                            for (final MemberEntity memberEntity : memberEntities) {
                                Log.d(
                                        TAG,
                                        "Inserting new information about member " +
                                                memberEntity.getId() +
                                                "into the db. This is the new info: " +
                                                "(id: " +
                                                memberEntity.getId() +
                                                ", office id: " +
                                                memberEntity.getOfficeId() +
                                                ", Name: " +
                                                memberEntity.getFirstName() +
                                                " " +
                                                memberEntity.getLastName() +
                                                ")"
                                );
                            }

                            db.memberDao().insertMembers(memberEntities.toArray(new MemberEntity[0]));
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        members -> members.forEach(
                                memberEntity -> refreshMember(memberEntity.getId())
                        ),
                        throwable -> Log.e(TAG, "Could not refresh all members of office with id "
                                + officeId, throwable)
                );
    }
}
