package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
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

    @SuppressLint("CheckResult")
    public void setStatus(final int memberId, final String newStatus) {
        modify(
                memberId,
                prevMember -> new MemberEntity(
                    prevMember.getId(),
                    prevMember.getOfficeId(),
                    prevMember.getFirstName(),
                    prevMember.getLastName(),
                    newStatus,
                    prevMember.getPicture(),
                    prevMember.getPictureMD5(),
                    prevMember.getServerAuthCode(),
                    prevMember.getCalendarId()
                )
        )
                .subscribe(
                        () -> Log.d(TAG, "Successfully set status of member " + memberId + " to " + newStatus),
                        throwable -> Log.e(TAG, "Setting a new status failed.", throwable)
                );
    }

    public Completable setServerAuthCode(final int memberId, final String serverAuthCode, final String eMail) {
        // TODO: Honor EMail
        return modify(
                memberId,
                prevMember -> new MemberEntity(
                        prevMember.getId(),
                        prevMember.getOfficeId(),
                        prevMember.getFirstName(),
                        prevMember.getLastName(),
                        prevMember.getStatus(),
                        prevMember.getPicture(),
                        prevMember.getPictureMD5(),
                        serverAuthCode,
                        prevMember.getCalendarId()
                )
        );
    }

    public Completable setCalendarId(int memberId, String calendarId) {
        return modify(
                memberId,
                prevMember -> new MemberEntity(
                        prevMember.getId(),
                        prevMember.getOfficeId(),
                        prevMember.getFirstName(),
                        prevMember.getLastName(),
                        prevMember.getStatus(),
                        prevMember.getPicture(),
                        prevMember.getPictureMD5(),
                        prevMember.getServerAuthCode(),
                        calendarId
                )
        );
    }

    private Completable modify(final int memberId, final Function<MemberEntity, MemberEntity> modifier) {
        return db
                .memberDao()
                .loadOnce(memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify member " + memberId + ", since the member could not be found in the database.", throwable)
                )
                .flatMap(
                        member -> api
                                .patchMember(
                                        auth.id,
                                        member.getId(),
                                        modifier.apply(member)
                                )
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Could not modify member " + memberId + ", since the new data could not be uploaded.", throwable)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement();
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
