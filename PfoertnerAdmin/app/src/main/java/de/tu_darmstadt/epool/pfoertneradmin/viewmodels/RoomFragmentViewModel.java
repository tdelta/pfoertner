package de.tu_darmstadt.epool.pfoertneradmin.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Office;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import io.reactivex.disposables.CompositeDisposable;

public class RoomFragmentViewModel extends AndroidViewModel {
    private static final String TAG = "RoomFragmentViewModel";

    private int officeId;
    private LiveData<String> currentRoomListener;
    private PfoertnerRepository repo;

    private CompositeDisposable disposables;

    /**
     * Constructor of the RoomFragmentViewModel class.
     * Initializes repo.
     *
     * @param rawApp PfoertnerApplication
     */
    public RoomFragmentViewModel(final Application rawApp) {
        super(rawApp);

        final PfoertnerApplication app = PfoertnerApplication.get(rawApp.getApplicationContext());

        repo = app.getRepo();
    }

    /**
     * This method initializes the RoomFragmentViewModel
     *
     * @param officeId of the given office
     */
    @SuppressWarnings("unchecked")
    public void init(final int officeId) {
        if (this.currentRoomListener != null) {
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

    /**
     * getter for currentRoomListener
     *
     * @return observable currentRoomListener
     */
    public LiveData<String> getCurrentRoomListener() {
        return currentRoomListener;
    }

    /**
     * setter for room
     *
     * @param room which will be set
     */
    public void setRoom(String room) {
        if (!room.trim().equals("")) {
            disposables.add(
                repo
                    .getOfficeRepo()
                    .setRoom(officeId, room)
                    .subscribe(
                            () -> Log.d(TAG, "Successfully set room to " + room),
                            throwable -> Log.e(TAG, "Setting a new room failed.", throwable)
                    )
            );
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.dispose();
    }
}
