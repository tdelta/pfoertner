package de.tu_darmstadt.epool.pfoertnerpanel.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.MemberEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;

import java.util.List;

/**
 * OfficeViewModel is a class that is responsible for preparing and managing the
 * member data for an Activity
 *
 */
public class OfficeViewModel extends AndroidViewModel {

    private LiveData<? extends Office> office;
    private PfoertnerRepository repo;

    public OfficeViewModel(final Application app){

        super(app);

        this.repo = PfoertnerApplication.get(getApplication().getApplicationContext()).getRepo();
    }

    /**
     * Initialize the VieModel for an office
     * @param officeId the office identifier
     */
    public void init(final int officeId) {
        if (this.office != null) {
            // ViewModel is created on a per-Fragment basis, so the memberId
            // doesn't change.
            return;
        }

        this.office = repo.getOfficeRepo().getOffice(officeId);
    }

    /**
     * return the office LiveData
     */
    public LiveData<? extends Office> getOffice() {
        return office;
    }

    /**
     * return the office LiveData
     * @param officeId the office identifier
     * @param officeId the office identifier
     */
    public LiveData<List<Member>> getOfficeMembers(final int officeId){
        return repo
                .getMemberRepo()
                .getMembersFromOffice(officeId);
    }
}
