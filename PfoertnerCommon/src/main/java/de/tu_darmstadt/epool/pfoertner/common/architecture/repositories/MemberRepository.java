package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.util.List;

public class MemberRepository {
    private static final String TAG = "MemberRepository";

    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;

    public MemberRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.api = api;
        this.auth = auth;
        this.db = db;
    }

    public LiveData<? extends Member> getMember(final int memberId) {
        refreshMember(memberId);

        return db.memberDao().load(memberId);
    }

    public LiveData<List<MemberEntity>> getMembersFromOffice(final int officeId){
        refreshAllMembersFromOffice(officeId);

        return db.memberDao().getAllMembersFromOffice(officeId);
    }

    public Flowable<? extends Member> getMemberFlowable(final int memberId) {
        refreshMember(memberId);

        return db.memberDao().loadFlowable(memberId);
    }

    @SuppressLint("CheckResult")
    public void setStatus(final int memberId, final String newStatus) {
        db
                .memberDao()
                .loadOnce(memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not set status of member " + memberId + ", since the member could not be found in the database.", throwable)
                )
                .flatMap(
                        member -> api
                                .patchMember(auth.id, member.getId(), new MemberEntity(
                                        member.getId(),
                                        member.getOfficeId(),
                                        member.getFirstName(),
                                        member.getLastName(),
                                        newStatus
                                ))
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Could not set status of member " + memberId + ", since the new data could not be uploaded.", throwable)
                )
                .subscribe(
                        o -> {},
                        throwable -> Log.e(TAG, "Setting a new status failed.", throwable)
                );
    }

    @SuppressLint("CheckResult")
    public void refreshMember(final int memberId) {
        api
                .getMember(auth.id, memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        memberEntity -> db.memberDao().upsert(memberEntity)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        memberEntity -> {},
                        throwable -> Log.e(TAG, "Could not refresh member.", throwable)
                );
    }

    @SuppressLint("CheckResult")
    public void refreshAllMembers() {
        Single
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

    @SuppressLint("CheckResult")
    public void refreshAllMembersFromOffice(final int officeId) {
        api
                .getMembersFromOffice(auth.id,officeId)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(
                        memberEntities -> db.memberDao().insertMembers(memberEntities.toArray(new MemberEntity[0]))
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
