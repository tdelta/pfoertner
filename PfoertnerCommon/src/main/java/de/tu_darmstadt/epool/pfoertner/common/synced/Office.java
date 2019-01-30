package de.tu_darmstadt.epool.pfoertner.common.synced;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeJoinData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.Observable;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.OfficeObserver;

public class Office extends Observable<OfficeObserver> {
    private final static String TAG = "Office";

    private final int id;
    private String joinCode;
    private String status;
    private List<Member> members;

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

    private static OfficeData loadOfficeFromLocalStorage(final SharedPreferences preferences) {
        return new OfficeData(
                preferences.getInt("OfficeId", -1),
                preferences.getString("OfficeJoinData", ""),
                preferences.getBoolean("OfficeStatusIsValid", false) ?
                          preferences.getString("OfficeStatus", null)
                        : null
        );
    }

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

    static void writeMembersToLocalStorage(final SharedPreferences preferences, final MemberData[] data) {
        final Gson gson = new Gson();

        final SharedPreferences.Editor e = preferences.edit();
        final String memberJson =  gson.toJson(data);

        e.putString("OfficeMembers", memberJson);

        e.commit();
    }

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

    private class DownloadMembersTask extends RequestTask<MemberData[]> {
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
        public MemberData[] doRequests() throws Exception {
            final MemberData[] members = loadMembers(Office.this.id, settings, service, auth);

            return members;
        }

        @Override
        protected void onSuccess(final MemberData[] updatedMembersData) {
            writeMembersToLocalStorage(settings, updatedMembersData);

            Office.this.setMembers(updatedMembersData);

            Office.this.notifyEachObserver(
                    OfficeObserver::onMembersChanged
            );
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to download new office data.");
        }
    }

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

    MemberData[] membersToData() {
        return this.members
                .stream()
                .map(Member::toData)
                .toArray(MemberData[]::new);
    }

    private Office(final OfficeData data, final MemberData[] members) {
        super();

        this.id = data.id;
        this.joinCode = data.joinCode;

        this.setMembers(members);
    }

    private void setMembers(final MemberData[] members) {
        this.members = Arrays.stream(members)
                .map(memberData -> new Member(this, memberData))
                .collect(Collectors.toList());
    }

    public void updateAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        this.downloadOfficeTask.whenDone(
                aVoid -> downloadOfficeTask.execute(preferences, service, auth)
        );
    }

    public void updateMembersAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        this.downloadMembersTask.whenDone(
                aVoid -> downloadMembersTask.execute(preferences, service, auth)
        );
    }

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

    public static Office createOffice(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
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

        return new Office(officeData, members);
    }

    public static Office loadOffice(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        final int officeID = preferences.getInt("OfficeId", -1);
        if (officeID == -1){
            throw new RuntimeException("OfficeData could not be loaded. Invalid officeId was loaded.");
        }

        return Office.loadOffice(
                officeID,
                preferences,
                service,
                auth
        );
    }

    public static Office loadOffice(final int officeId, final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
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

        return new Office(officeData, members);
    }

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

    public static MemberData joinOffice(final int officeId, final String joinCode, String firstName, String lastName, SharedPreferences settings, PfoertnerService service, Authentication authtoken)  {
        return new ResourceInitProtocol<MemberData>(
    "Could not join office. Do you have an internet connection?"
        ) {
            @Override
            protected MemberData tryLoadFromServer() throws Exception {
                final MemberData memberData = service.joinOffice(
                        authtoken.id,
                        officeId,
                        new OfficeJoinData(
                                joinCode,
                                firstName,
                                lastName
                        )
                ).execute().body();

                return memberData;
            }
        }.execute();
    }

    public static boolean hadBeenRegistered(final SharedPreferences settings) {
        return settings.contains("OfficeId");
    }

    static class ResourceInitProtocol<T> {
        private final String errorMsg;

        public ResourceInitProtocol() {
            this.errorMsg = null;
        }

        public ResourceInitProtocol(final String errorMsg) {
            this.errorMsg = errorMsg;
        }

        protected T tryLoadFromServer() throws Exception {
            return null;
        }

        protected T tryLoadFromStorage() throws Exception {
            return null;
        }

        protected void saveToStorage(final T data) { }

        public final T execute() {
            T data;

            try {
                data = tryLoadFromServer();

                if (data != null) {
                    saveToStorage(data);
                }
            } catch (final Exception e) {
                e.printStackTrace();

                data = null;
            }

            try {
                if (data == null) {
                    data = tryLoadFromStorage();
                }
            } catch (final Exception e) {
                e.printStackTrace();

                data = null;
            }

            if (data == null) {
                throw new RuntimeException(
                        this.errorMsg == null ?
                                  "Could not load resource. Neither from the server nor from local storage."
                                : this.errorMsg
                );
            }

            return data;
        }
    }
}

