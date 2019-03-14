package de.tu_darmstadt.epool.pfoertner.common.architecture.repositories;

import android.util.Log;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;

/**
 * Simply stores references to all data repositories.
 * <p>
 * For information on the purpose of the repositories consult their respective documentation.
 */
public class PfoertnerRepository {
    private static final String TAG = "PfoertnerRepository";

    /**
     * ATTENTION: When adding a repository, please also add a refresh function for all data, see
     * refreshAllData() in this class
     **/
    private final DeviceRepository deviceRepo;
    private final OfficeRepository officeRepo;
    private final MemberRepository memberRepo;
    private final AppointmentRepository appointmentRepository;
    private final InitStatusRepository initStatusRepo;

    /**
     * Creates an instance of this class and also instances of all available types of
     * repositories which are used by both Apps (PfoertnerAdmin and PfoertnerPanel).
     *
     * @param api  used for network calls if information is not available locally or needs to
     *             refreshed
     * @param auth authentication data necessary to use network api
     * @param db   database where data is being cached
     */
    public PfoertnerRepository(final PfoertnerApi api, final Authentication auth,
                               final AppDatabase db) {
        this.deviceRepo = new DeviceRepository(api, auth, db);
        this.officeRepo = new OfficeRepository(api, auth, db);
        this.memberRepo = new MemberRepository(api, auth, db);
        this.initStatusRepo = new InitStatusRepository(db);
        this.appointmentRepository = new AppointmentRepository(api, auth, db);
    }

    public DeviceRepository getDeviceRepo() {
        return deviceRepo;
    }

    public OfficeRepository getOfficeRepo() {
        return officeRepo;
    }

    public MemberRepository getMemberRepo() {
        return memberRepo;
    }

    public InitStatusRepository getInitStatusRepo() {
        return initStatusRepo;
    }

    public AppointmentRepository getAppointmentRepository() {
        return appointmentRepository;
    }


    /**
     * Instructs repositories to refresh their cached data.
     *
     * Currently it supports the device, office and member repositories.
     */
    public void refreshAllLocalData() {
        Log.d(TAG, "Refreshing all local data asynchronously.");

        deviceRepo.refreshAllLocalData();
        officeRepo.refreshAllLocalData();
        memberRepo.refreshAllLocalData();
    }
}
