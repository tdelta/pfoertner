package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Executor;

import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class PfoertnerRepository {
    private static final String TAG = "PfoertnerRepository";

    private final OfficeRepository officeRepo;
    private final MemberRepository memberRepo;

    public PfoertnerRepository(final PfoertnerApi api, final Authentication auth, final AppDatabase db) {
        this.officeRepo = new OfficeRepository(api, auth, db);
        this.memberRepo = new MemberRepository(api, auth, db);
    }

    public OfficeRepository getOfficeRepo() {
        return officeRepo;
    }

    public MemberRepository getMemberRepo() {
        return memberRepo;
    }
}
