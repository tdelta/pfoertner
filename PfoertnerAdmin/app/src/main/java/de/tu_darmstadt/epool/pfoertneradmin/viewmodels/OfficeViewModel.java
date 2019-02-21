package de.tu_darmstadt.epool.pfoertneradmin.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;

public class OfficeViewModel extends AndroidViewModel {
    private LiveData<? extends Office> office;
    private PfoertnerRepository repo;

    public OfficeViewModel(final Application app) {
        super(app);

        this.repo = AdminApplication.get(getApplication().getApplicationContext()).getRepo();
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
}
