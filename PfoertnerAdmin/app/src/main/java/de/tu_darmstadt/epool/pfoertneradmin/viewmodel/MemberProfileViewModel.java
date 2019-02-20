package de.tu_darmstadt.epool.pfoertneradmin.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertneradmin.model.Member;
import de.tu_darmstadt.epool.pfoertneradmin.repositories.PfoertnerRepository;

public class MemberProfileViewModel extends AndroidViewModel {
    private LiveData<? extends Member> member;
    private PfoertnerRepository repo;

    public MemberProfileViewModel(final Application app) {
        super(app);

        this.repo = AdminApplication.get(getApplication().getApplicationContext()).getRepo();
    }

    public void init(final int memberId) {
        if (this.member != null) {
            // ViewModel is created on a per-Fragment basis, so the userId
            // doesn't change.
            return;
        }

        this.member = repo.getMember(memberId);
    }

    public LiveData<? extends Member> getMember() {
        return member;
    }
}
