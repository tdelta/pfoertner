package de.tu_darmstadt.epool.pfoertneradmin.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import io.reactivex.disposables.CompositeDisposable;

public class GlobalStatusFragmentViewModel extends AndroidViewModel {
    private static final String TAG = "GlobalStatusFragmentViewModel";

    private int officeId;
    private LiveData<Integer> currentOfficeStatusIdx;
    private int newIdx = 0;
    private PfoertnerRepository repo;

    private List<String> statusList;

    private CompositeDisposable disposables;

    /**
     * Constructor of the GlobalStatusFragmentViewModel class.
     * Initializes repo.
     *
     * @param rawApp PfoertnerApplication
     */
    public GlobalStatusFragmentViewModel(final Application rawApp) {
        super(rawApp);

        final PfoertnerApplication app = PfoertnerApplication.get(rawApp.getApplicationContext());

        repo = app.getRepo();
    }

    /**
     * This method initializes the GlobalStatusFragment.
     *
     * @param officeId of the given office
     */
    @SuppressWarnings("unchecked")
    public void init(final int officeId) {
        if (this.currentOfficeStatusIdx != null) {
            // ViewModel is created on a per-Fragment basis, so the officeId
            // doesn't change.
            return;
        }

        if (disposables != null) {
            disposables.dispose();
        }
        disposables = new CompositeDisposable();

        // Initialize live data
        this.officeId = officeId;

        final MediatorLiveData<Integer> liveDataConverter = new MediatorLiveData<>();
        liveDataConverter.addSource(
                repo
                        .getOfficeRepo()
                        .getOffice(officeId),
                (Observer) officeObj -> {
                    final Office office = (Office) officeObj;

                    if (office != null && office.getStatus() != null) {
                        if (!this.statusList.contains(office.getStatus())) {
                            addToStatusList(office.getStatus());
                        }

                        liveDataConverter.setValue(
                                statusToIdx(office.getStatus())
                        );
                    }

                    else {
                        liveDataConverter.setValue(0);
                    }
                }
        );


        this.currentOfficeStatusIdx = liveDataConverter;

        // initialize status list
        final PfoertnerApplication app = PfoertnerApplication.get(getApplication().getApplicationContext());
        final SharedPreferences settings = app.getSettings();

        if (settings.contains("officeStatusModes")){
            Gson gson = new Gson();
            String statusJSON =  settings.getString("officeStatusModes", null);
            statusList = new ArrayList<>(Arrays.asList(gson.fromJson(statusJSON, String[].class)));
        }

        else {
            statusList = new ArrayList<>();
            statusList.add("Come In!");
            statusList.add("Only Urgent Matters!");
            statusList.add("Do Not Disturb!");

        }
    }

    /**
     * This method transforms status strings to their
     * corresponding idxs.
     *
     * @param str status string
     * @return to the given status string corresponding idx
     */
    private int statusToIdx(final String str) {
        final int i = statusList.indexOf(str);

        if (i < 0) {
            return 0;
        }

        else {
            return i;
        }
    }

    /**
     * Getter for currentOfficeStatusIdx
     *
     * @return currentOfficeStatusIdx
     */
    public LiveData<Integer> getCurrentOfficeStatusIdx() {
        return currentOfficeStatusIdx;
    }

    /**
     * Setter for NewIdx
     *
     * @param newIdx which will be set
     */
    public void setNewIdx(final int newIdx) {
        this.newIdx = newIdx;
    }

    /**
     * Getter for NewIdx
     *
     * @return newIdx
     */
    public int getNewIdx() {
        return this.newIdx;
    }

    /**
     * Gette for the currently selected status
     *
     * @return currently selected status
     */
    public String getSelectedStatus() {
        return this.statusList.get(this.getNewIdx());
    }

    /**
     * Getter of statusList
     *
     * @return list of the currently available status
     */
    public List<String> getStatusList() {
        return statusList;
    }

    /**
     * This method is called, in order to add a newly created
     * status to the list of status
     *
     * @param text new office status
     */
    public void addToStatusList(final String text) {
        if (!text.trim().equals("") && !statusList.contains(text.trim())) {
            final PfoertnerApplication app = PfoertnerApplication.get(getApplication().getApplicationContext());

            statusList.add(text.trim());

            final Gson gson = new Gson();

            final SharedPreferences.Editor e = app.getSettings().edit();
            e.putString("officeStatusModes", gson.toJson(statusList));
            e.apply();
        }
    }

    /**
     * This method sets the global office status
     *
     */
    public void setStatus() {
        final String newStatus = statusList.get(getNewIdx());

        disposables.add(
            repo
                    .getOfficeRepo()
                    .setStatus(officeId, newStatus)
                    .subscribe(
                            () -> Log.d(TAG, "Successfully set status to " + newStatus),
                            throwable -> Log.e(TAG, "Setting a new status failed.", throwable)
                    )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.dispose();
    }
}
