package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
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
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MemberRepository {
    private static final String TAG = "MemberRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    private static Member toInterface(final MemberEntity entity) {
        return entity; //  implicit cast to interface
    }

    public MemberRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    public LiveData<Member> getMember(final int memberId) {
        refreshMember(memberId);

        return Transformations.map(
                db.memberDao().load(memberId),
                MemberRepository::toInterface
        );
    }

    public LiveData<List<Member>> getMembersFromOffice(final int officeId){
        refreshAllMembersFromOffice(officeId);

        return
                Transformations.map(
                        db.memberDao().getAllMembersFromOffice(officeId),
                        ArrayList<Member>::new // implicit conversion to pure interface type
                );
    }

    public Flowable<Member> getMemberFlowable(final int memberId) {
        refreshMember(memberId);

        return db
                .memberDao()
                .loadFlowable(memberId)
                .map(MemberRepository::toInterface);
    }

    public Single<Member> getMemberOnce(final int memberId){
        return db.memberDao()
                .loadOnce(memberId)
                .map(MemberRepository::toInterface);
    }

    public Completable setStatus(final int memberId, final String newStatus) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setStatus(newStatus)
        );
    }

    public Completable setFirstName(final int memberId, final String firstName) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setFirstName(firstName)
        );
    }

    public Completable setLastName(final int memberId, final String lastName) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setLastName(lastName)
        );
    }

    public Completable setServerAuthCode(final int memberId, final String serverAuthCode, final String eMail) {
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

    public Completable setCalendarId(int memberId, String calendarId) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setCalendarId(calendarId)
        );
    }

    public Completable setWebhookId(int memberId, String webhookId) {
        return modify(
                memberId,
                prevMemberClone -> prevMemberClone.setWebhookId(webhookId)
        );
    }

    private Completable modify(final int memberId, final Consumer<MemberEntity> modifier) {
        return db
                .memberDao()
                .loadOnce(memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify member " + memberId + ", since the member could not be found in the database.", throwable)
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
                        throwable -> Log.e(TAG, "Could not modify member " + memberId + ", since the new data could not be uploaded.", throwable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement();
    }

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
                        memberEntity -> {},
                        throwable -> Log.e(TAG, "Could not refresh member with id " + memberId, throwable)
                );
    }

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

    public Disposable refreshAllMembersFromOffice(final int officeId) {
        Log.d(TAG, "About to refresh all data for members of office " + officeId + "...");

        return api
                .getMembersFromOffice(auth.id,officeId)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(
                        memberEntities -> {
                            Log.d(
                                    TAG,
                                    "Successfully retrieved new information about all members of office " +
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
                        throwable -> Log.e(TAG, "Could not refresh all members of office with id " + officeId, throwable)
                );
    }
}
