package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

    @SuppressLint("CheckResult")
    public void refreshMember(final int memberId) {
        api
                .getMember(auth.id, memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess(
                        memberEntity -> {
                            final OfficeEntity oe = db.officeDao().load(1).getValue();
                            final int x = db.officeDao().countEntities(0);
                            db.memberDao().save(memberEntity);
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        memberEntity -> {},
                        throwable -> Log.e(TAG, "Could not refresh member.", throwable)
                );
    }
}
