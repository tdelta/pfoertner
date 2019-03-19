package de.tu_darmstadt.epool.pfoertner.common.synced;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.helpers.ResourceInitProtocol;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.Observable;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.OfficeObserver;

/**
 * Old office class that can save and load office data in local storage, send modified data to the server
 * and propagate updates in the office to observers
 *
 * Use instead: {@link de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.OfficeRepository}
 */
@Deprecated
public class Office extends Observable<OfficeObserver> {
    private final static String TAG = "Office";

    private final int id;
    private String joinCode;
    private String status;
    private List<Member> members = new ArrayList<>(0);

    public int getId() {
        return id;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public String getStatus() {
        return status;
    }

    public List<Member> getMembers() { return members; }

    public Optional<Member> getMemberById(final int id) {
        return getMembers()
                .stream()
                .filter(member -> member.getId() == id)
                .findFirst();
    }

    /**
     * Writes the office data to local settings
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param data Data to save
     */
    private static void writeOfficeToLocalStorage(final SharedPreferences preferences, final OfficeData data) {
        final SharedPreferences.Editor e = preferences.edit();

        e.putInt("OfficeId", data.id);
        e.putString("OfficeJoinData", data.joinCode);
        e.putBoolean("OfficeStatusIsValid", data.status != null);

        if (data.status != null) {
            e.putString("OfficeStatus", data.status);
        }

        e.apply();
    }

    /**
     * Loads the office data from local settings
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @return Loaded Data
     */
    private static OfficeData loadOfficeFromLocalStorage(final SharedPreferences preferences) {
        return new OfficeData(
                preferences.getInt("OfficeId", -1),
                preferences.getString("OfficeJoinData", ""),
                preferences.getBoolean("OfficeStatusIsValid", false) ?
                          preferences.getString("OfficeStatus", null)
                        : null
        );
    }

    /**
     * Loads office member data from local settings
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @return Loaded member data
     */
    private static MemberData[] loadMembersFromLocalStorage(final SharedPreferences preferences) {
        final Gson gson = new Gson();

        final String memberJson =  preferences.getString("OfficeMembers", null);

        if (memberJson == null) {
            return null;
        }

        else {
            return gson.fromJson(memberJson, MemberData[].class);
        }
    }

    /**
     * Writes data of office members into local settings
     * @param preferences local settings (PfoertnerApplication.getSettings())
     * @param data Member data to save
     */
    static void writeMembersToLocalStorage(final SharedPreferences preferences, final MemberData[] data) {
        final Gson gson = new Gson();

        final SharedPreferences.Editor e = preferences.edit();
        final String memberJson =  gson.toJson(data);

        e.putString("OfficeMembers", memberJson);

        e.commit();
    }

    /**
     * Requests office data from the server (Office members not included) on an io thread
     * If the load was successful the loaded data is written into memory.
     * If the data changed, all observers of the office are notified of the changes.
     */
    private class DownloadOfficeTask extends RequestTask<OfficeData> {
        // TODO: Refactor! Shares many similarities with the other methods
        private SharedPreferences settings;
        private PfoertnerService service;
        private Authentication auth;

        public void execute(final SharedPreferences settings, final PfoertnerService service, final Authentication auth) {
            this.settings = settings;
            this.service = service;
            this.auth = auth;

            super.execute();
        }

        @Override
        public OfficeData doRequests() throws Exception {
            final OfficeData updatedOfficeData = service
                    .loadOffice(this.auth.id, Office.this.id)
                    .execute()
                    .body();

            return updatedOfficeData;
        }

        @Override
        protected void onSuccess(final OfficeData updatedOfficeData) {
            writeOfficeToLocalStorage(this.settings, updatedOfficeData);

            final String oldJoinCode = Office.this.joinCode;
            final String oldStatus = Office.this.status;

            Office.this.joinCode = updatedOfficeData.joinCode;
            if (!oldJoinCode.equals(updatedOfficeData.joinCode)) {
                Office.this.notifyEachObserver(officeObserver -> officeObserver.onJoinCodeChanged(Office.this.joinCode));
            }

            Office.this.status = updatedOfficeData.status;
            if (   updatedOfficeData.status == null && oldStatus != null
                || updatedOfficeData.status != null && oldStatus == null
                || updatedOfficeData.status != null && oldStatus != null && !oldStatus.equals(updatedOfficeData.status)
            ) {
                Office.this.notifyEachObserver(officeObserver -> officeObserver.onStatusChanged(Office.this.status));
            }
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to download new office data.");
        }
    }

    /**
     * Requests office member data for all members of this office from the server on an io thread
     * If the load was successful the loaded data is written into memory.
     * If the data changed, all observers of the office are notified of the changes.
     */
    private class DownloadMembersTask extends RequestTask<MemberData[]> {
        // TODO: Refactor! Shares many similarities with the other methods
        private SharedPreferences settings;
        private PfoertnerService service;
        private Authentication auth;
        private File filesDir;

        public void execute(final SharedPreferences settings, final PfoertnerService service, final Authentication auth, final File filesDir) {
            this.settings = settings;
            this.service = service;
            this.auth = auth;
            this.filesDir = filesDir;

            super.execute();
        }

        @Override
        public MemberData[] doRequests() throws Exception {
            final MemberData[] members = loadMembers(Office.this.id, settings, service, auth);

            return members;
        }

        @Override
        protected void onSuccess(final MemberData[] updatedMembersData) {
            writeMembersToLocalStorage(settings, updatedMembersData);

            Office.this.setMembers(settings, service, auth, filesDir, updatedMembersData);
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to download new office data.");
        }
    }

    /**
     * Uploads current office data to the server on an io thread.
     */
    private class UploadOfficeTask extends RequestTask<Void> {
        // TODO: Refactor! Shares many similarities with the other methods
        private PfoertnerService service;
        private Authentication auth;
        private OfficeData data;

        public void execute(final PfoertnerService service, final Authentication auth, final OfficeData data) {
            this.service = service;
            this.auth = auth;
            this.data = data;

            super.execute();
        }

        @Override
        public Void doRequests() throws Exception {
            service.updateOfficeData(
                    this.auth.id,
                    this.data.id,
                    this.data
            ).execute();

            return null;
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to update office data on the server.");
        }
    }

    private final DownloadOfficeTask downloadOfficeTask = new DownloadOfficeTask();
    private final UploadOfficeTask uploadOfficeTask = new UploadOfficeTask();
    private final DownloadMembersTask downloadMembersTask = new DownloadMembersTask();

    /**
     * Converts the member objects of this office to MemberData objects, that can be used in a server call
     * @return Array of MemberData
     */
    MemberData[] membersToData() {
        return this.members
                .stream()
                .map(Member::getMemberData)
                .toArray(MemberData[]::new);
    }

    /**
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     * @param filesDir Directory to load/ save metadata like pictures for office members
     * @param data Underlying data to fill this wrapper
     * @param members Underlying data to fill this wrapper
     */
    private Office(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir, final OfficeData data, final MemberData[] members) {
        super();

        this.id = data.id;
        this.joinCode = data.joinCode;
        this.status = data.status;

        this.setMembers(preferences, service, auth, filesDir, members);
    }

    /**
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     * @param filesDir Directory to load/ save metadata like pictures for office members
     * @param members Underlying data to fill this wrapper
     */
    private void setMembers(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir, final MemberData[] members) {
        Log.d(TAG, "Office members are being reset. There have been " + this.members.size() + " members before and there will be " + members.length + " members after this.");

        final List<MemberData> updatedMembersDataList = new ArrayList<>(Arrays.asList(members));

        final List<Member> replacementList = new ArrayList<>(members.length);
        final List<Member> newMembers = new LinkedList<>();
        final List<Integer> removedMembers = new LinkedList<>();

        // collect removed member ids and update the ones still existing
        for (final Member oldMember : this.members) {
            final int newDataIdx = Iterables.indexOf(
                    updatedMembersDataList,
                    input -> input.id == oldMember.getId()
                );

            if (newDataIdx < 0) {
                removedMembers.add(oldMember.getId());
            }

            else {
                final MemberData updatedData = updatedMembersDataList.remove(newDataIdx);

                if (!oldMember.downloadPictureIfNecessary(preferences, service, auth, updatedData, filesDir)) {
                    oldMember.updateByData(updatedData);
                }

                replacementList.add(oldMember);
            }
        }

        // all remaining elements in updatedMembersDataList are new members
        for (final MemberData data : updatedMembersDataList) {
            final Member member = new Member(this, data);
            member.downloadPictureIfNecessary(preferences, service, auth, data, filesDir);
            newMembers.add(member);

            replacementList.add(member);
        }

        Log.d(TAG, "There have " + newMembers.size() + " new members been added and " + removedMembers.size() + " members been removed. The new number of members is " + replacementList.size());

        this.members = replacementList;
        this.notifyEachObserver(officeObserver -> officeObserver.onMembersChanged(newMembers, removedMembers));
    }

    /**
     * Requests office data from the server (Office members not included) on an io thread
     * If the load was successful the loaded data is written into memory.
     * If the data changed, all observers of the office are notified of the changes.
     *
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     */
    public void updateAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        this.downloadOfficeTask.whenDone(
                aVoid -> downloadOfficeTask.execute(preferences, service, auth)
        );
    }
    /**
     * Requests office member data for all members of this office from the server on an io thread
     * If the load was successful the loaded data is written into memory.
     * If the data changed, all observers of the office are notified of the changes.
     *
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     */
    public void updateMembersAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir) {
        this.downloadMembersTask.whenDone(
                aVoid -> downloadMembersTask.execute(preferences, service, auth, filesDir)
        );
    }

    /**
     * Uploads office data with a changed status to the server
     *
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     * @param newStatus New status to upload
     */
    public void setStatus(final PfoertnerService service, final Authentication auth, final String newStatus) {
        final OfficeData data = new OfficeData(
                this.id,
                this.joinCode,
                newStatus
        );

        this.uploadOfficeTask.whenDone(
               aVoid -> this.uploadOfficeTask.execute(
                       service,
                       auth,
                       data
               )
        );
    }

    /**
     * Loads office data including office members from local settings or if no office data exists,
     * asks server to create a new office and saves it.
     *
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     * @param filesDir Directory to load/ save metadata like pictures for office members
     * @return Office
     */
    public static Office createOffice(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir) {
        final OfficeData officeData;

        if (preferences.contains("OfficeId") /*officeData already registered*/) {
            officeData = loadOfficeFromLocalStorage(preferences);
        }

        else {
            officeData = new ResourceInitProtocol<OfficeData>(
                    "Could not create a new officeData. Do you have an internet connection?"
            ) {
                @Override
                protected OfficeData tryLoadFromServer() throws Exception {
                    return service
                            .createOffice(auth.id)
                            .execute()
                            .body();
                }

                @Override
                protected void saveToStorage(final OfficeData data) {
                    writeOfficeToLocalStorage(preferences, data);
                }
            }.execute();
        }

        final MemberData[] members = loadMembers(officeData.id, preferences, service, auth);

        return new Office(preferences, service, auth, filesDir, officeData, members);
    }

    /**
     * Loads office data for this office from the server or from local settings if loading from the server fails.
     *
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     * @param filesDir Directory to load/ save metadata like pictures for office members
     * @throws RuntimeException if no office data was ever created or the settings are invalid
     * @return
     */
    public static Office loadOffice(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir) {
        final int officeID = preferences.getInt("OfficeId", -1);
        if (officeID == -1){
            throw new RuntimeException("OfficeData could not be loaded. Invalid officeId was loaded.");
        }

        return Office.loadOffice(
                officeID,
                preferences,
                service,
                auth,
                filesDir
        );
    }

    /**
     * Loads office data for an office from the server or from local settings if loading from the server fails.
     *
     * @param officeId Id of the office to load
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     * @param filesDir Directory to load/ save metadata like pictures for office members
     * @return Loaded office
     */
    public static Office loadOffice(final int officeId, final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir) {
        final OfficeData officeData = new ResourceInitProtocol<OfficeData>() {
            @Override
            protected OfficeData tryLoadFromServer() throws Exception {
                return service
                        .loadOffice(auth.id, officeId)
                        .execute()
                        .body();
            }

            @Override
            protected OfficeData tryLoadFromStorage() throws Exception {
                Log.d(TAG, "Had to load officeData from local storage since we could not connect.");

                return loadOfficeFromLocalStorage(preferences);
            }

            @Override
            protected void saveToStorage(final OfficeData data) {
                writeOfficeToLocalStorage(preferences, data);
            }
        }.execute();

        final MemberData[] members = loadMembers(officeId, preferences, service, auth);

        return new Office(preferences, service, auth, filesDir, officeData, members);
    }

    /**
     * Loads office member data for an office from the server or from local settings if loading from the server fails.
     *
     * @param officeId Id of the office to load
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication to make server calls (PfoertnerApplication.getAuthentication())
     * @return Loaded member data
     */
    private static MemberData[] loadMembers(final int officeId, final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        return new ResourceInitProtocol<MemberData[]>(
                "Could not load members of office."
        ) {
            @Override
            protected MemberData[] tryLoadFromServer() throws Exception {
                return service.getOfficeMembers(
                        auth.id,
                        officeId
                ).execute().body();
            }

            @Override
            protected MemberData[] tryLoadFromStorage() {
                return loadMembersFromLocalStorage(preferences);
            }

            @Override
            protected void saveToStorage(final MemberData[] data) {
                writeMembersToLocalStorage(preferences, data);
            }
        }.execute();
    }

    /**
     * @param settings Local settings (PfoertnerApplication.getSettings())
     * @return True if an office was saved in local settings
     */
    public static boolean hadBeenRegistered(final SharedPreferences settings) {
        return settings.contains("OfficeId");
    }

}

