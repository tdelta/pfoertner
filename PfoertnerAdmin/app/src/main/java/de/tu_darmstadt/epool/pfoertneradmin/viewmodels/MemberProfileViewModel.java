package de.tu_darmstadt.epool.pfoertneradmin.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;

public class MemberProfileViewModel extends AndroidViewModel {
    private LiveData<? extends Member> member;
    private PfoertnerRepository repo;

    /**
     * Constructor of the MemverProfileViewModel class
     *
     * @param app Pfoertner Application
     */
    public MemberProfileViewModel(final Application app) {
        super(app);

        this.repo = AdminApplication.get(getApplication().getApplicationContext()).getRepo();
    }

    /**
     * This method initializes the member field
     *
     * @param memberId of the member to initialize
     */
    public void init(final int memberId) {
        if (this.member != null) {
            // ViewModel is created on a per-Fragment basis, so the memberId
            // doesn't change.
            return;
        }

        this.member = repo.getMemberRepo().getMember(memberId);
    }

    /**
     *  Getter for member LiveData
     *
     * @return observable Member object
     */
    public LiveData<? extends Member> getMember() {
        return member;
    }
}
