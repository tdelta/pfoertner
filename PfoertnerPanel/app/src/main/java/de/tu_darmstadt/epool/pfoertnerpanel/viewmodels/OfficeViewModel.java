package de.tu_darmstadt.epool.pfoertnerpanel.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;

import java.util.List;

public class OfficeViewModel extends AndroidViewModel {

    private LiveData<? extends Office> office;
    private PfoertnerRepository repo;
    //private LiveData<List<MemberEntity>> officemembers;

    public OfficeViewModel(final Application app){

        super(app);

        this.repo = PfoertnerApplication.get(getApplication().getApplicationContext()).getRepo();
    }

    public void init(final int officeId) {
        if (this.office != null) {
            // ViewModel is created on a per-Fragment basis, so the memberId
            // doesn't change.
            return;
        }

        this.office = repo.getOfficeRepo().getOffice(officeId);
    }

    public LiveData<? extends Office> getOffice() {
        return office;
    }


    public LiveData<List<MemberEntity>> getOfficeMembers(final int officeId){
        return repo.getMemberRepo().getMembersFromOffice(officeId);
    }
}
