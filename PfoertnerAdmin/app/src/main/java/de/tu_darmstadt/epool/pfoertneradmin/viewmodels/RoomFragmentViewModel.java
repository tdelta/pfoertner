package de.tu_darmstadt.epool.pfoertneradmin.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;

public class RoomFragmentViewModel extends AndroidViewModel {
    private int officeId;
    private LiveData<String> currentRoomListener;
    private PfoertnerRepository repo;


    public RoomFragmentViewModel(final Application rawApp) {
        super(rawApp);

        final PfoertnerApplication app = PfoertnerApplication.get(rawApp.getApplicationContext());

        repo = app.getRepo();
    }

    @SuppressWarnings("unchecked")
    public void init(final int officeId) {
        if (this.currentRoomListener != null) {
            // ViewModel is created on a per-Fragment basis, so the officeId
            // doesn't change.
            return;
        }

        // Initialize live data
        this.officeId = officeId;

        final MediatorLiveData<String> liveDataConverter = new MediatorLiveData<>();
        liveDataConverter.addSource(
                repo
                        .getOfficeRepo()
                        .getOffice(officeId),
                (Observer) officeObj -> {
                    final Office office = (Office) officeObj;

                    if (office != null && office.getRoom() != null) {
                        liveDataConverter.setValue(
                                office.getRoom()
                        );
                    }

                    else {
                        liveDataConverter.setValue("Not Set");
                    }
                }
        );
        this.currentRoomListener = liveDataConverter;
    }

    public LiveData<String> getCurrentRoomListener() {
        return currentRoomListener;
    }

    public void setRoom(String room) {
        repo
                .getOfficeRepo()
                .setRoom(officeId, room);
    }

}
