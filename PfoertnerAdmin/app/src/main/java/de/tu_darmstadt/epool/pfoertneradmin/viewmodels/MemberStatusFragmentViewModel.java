package de.tu_darmstadt.epool.pfoertneradmin.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import io.reactivex.disposables.CompositeDisposable;

public class MemberStatusFragmentViewModel extends AndroidViewModel {
    private static final String TAG = "MemberStatusFragmentViewModel";

    private int memberId;
    private LiveData<Integer> currentMemberStatusIdx;
    private int newIdx = 0;
    private PfoertnerRepository repo;

    private List<String> statusList;

    private CompositeDisposable disposables;

    /**
     * This is the constructor of the MemberStatusFragementViewModel
     * class.
     * Initializes repo.
     *
     * @param rawApp PfoertnerApplication
     */
    public MemberStatusFragmentViewModel(final Application rawApp) {
        super(rawApp);

        final PfoertnerApplication app = PfoertnerApplication.get(rawApp.getApplicationContext());

        repo = app.getRepo();
    }

    /**
     * This method initializes the MemberStatusFragment
     *
     * @param memberId of the given member
     */
    @SuppressWarnings("unchecked")
    public void init(final int memberId) {
        if (this.currentMemberStatusIdx != null) {
            // ViewModel is created on a per-Fragment basis, so the memberId
            // doesn't change.
            return;
        }

        if (this.disposables != null) {
            this.disposables.dispose();
        }
        this.disposables = new CompositeDisposable();

        // Initialize live data
        this.memberId = memberId;

        final MediatorLiveData<Integer> liveDataConverter = new MediatorLiveData<>();
        liveDataConverter.addSource(
                repo
                        .getMemberRepo()
                        .getMember(memberId),
                (Observer) memberObj -> {
                    final Member member = (Member) memberObj;

                    if (member != null && member.getStatus() != null) {
                        if (!this.statusList.contains(member.getStatus())) {
                            addToStatusList(member.getStatus());
                        }

                        liveDataConverter.setValue(
                                statusToIdx(member.getStatus())
                        );
                    }

                    else {
                        liveDataConverter.setValue(0);
                    }
                }
        );


        this.currentMemberStatusIdx = liveDataConverter;

        // initialize status list
        final PfoertnerApplication app = PfoertnerApplication.get(getApplication().getApplicationContext());
        final SharedPreferences settings = app.getSettings();

        if (settings.contains("memberStatusModes")){
            Gson gson = new Gson();
            String statusJSON =  settings.getString("memberStatusModes", null);
            statusList = new ArrayList<>(Arrays.asList(gson.fromJson(statusJSON, String[].class)));
        }

        else {
            statusList = new ArrayList<>();
            statusList.add("Available");
            statusList.add("In meeting");
            statusList.add("Out of office");

        }
    }

    /**
     * This method transforms member status to their corresponding
     * idxs.
     *
     * @param str status string which will be transformed
     * @return idx corresponding to the given status string
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
     * getter for currentMemberStatus
     *
     * @return observable currentMemberStatus
     */
    public LiveData<Integer> getCurrentMemberStatusIdx() {
        return currentMemberStatusIdx;
    }

    /**
     * setter for newIdx
     *
     * @param newIdx which will be set
     */
    public void setNewIdx(final int newIdx) {
        this.newIdx = newIdx;
    }

    /**
     * getter for newIdx
     *
     * @return newIdx
     */
    public int getNewIdx() {
        return this.newIdx;
    }

    /**
     * getter for the currently selected status
     *
     * @return the currently selected status
     */
    public String getSelectedStatus() {
        return this.statusList.get(this.getNewIdx());
    }

    /**
     * getter for statusList
     *
     * @return statusList
     */
    public List<String> getStatusList() {
        return statusList;
    }

    /**
     * This method adds a new status to the list of status.
     *
     * @param text new status, which will be added to the list
     */
    public void addToStatusList(final String text) {
        if (!text.trim().equals("") && !statusList.contains(text.trim())) {
            final PfoertnerApplication app = PfoertnerApplication.get(getApplication().getApplicationContext());

            statusList.add(text.trim());

            final Gson gson = new Gson();

            final SharedPreferences.Editor e = app.getSettings().edit();
            e.putString("memberStatusModes", gson.toJson(statusList));
            e.apply();
        }
    }

    /**
     * setter for the personal status
     *
     */
    public void setStatus() {
        final String newStatus = statusList.get(getNewIdx());

        disposables.add(
            repo
                .getMemberRepo()
                .setStatus(memberId, newStatus)
                .subscribe(
                        () -> Log.d(TAG, "Successfully set status of member " + memberId + " to " + newStatus),
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
