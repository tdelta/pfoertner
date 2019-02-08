package de.tu_darmstadt.epool.pfoertner.common.synced;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.annotations.Expose;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeJoinData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.helpers.ResourceInitProtocol;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.Observable;

public class Member extends Observable<MemberObserver> {
    private static final String TAG = "Member";

    private final int id;
    private String lastName;
    private String firstName;
    private String status;
    private String accessToken;

    private String serverAuthCode;

    private final Office office;

    private final DownloadMemberTask downloadMemberTask = new DownloadMemberTask();
    private final UploadMemberTask uploadMemberTask = new UploadMemberTask();

    Member(final Office office, final MemberData data) {
        super();

        this.id = data.id;
        this.lastName = data.lastName;
        this.firstName = data.firstName;
        this.office = office;
    }

    public static MemberData joinOffice(final int officeId, final String joinCode, String firstName, String lastName, String status, SharedPreferences settings, PfoertnerService service, Authentication authtoken)  {
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
                                lastName,
                                status
                        )
                ).execute().body();

                return memberData;
            }

            @Override
            protected void saveToStorage(MemberData data) {
                final SharedPreferences.Editor e = settings.edit();

                e.putInt("MemberId", data.id);

                e.commit();
            }
        }.execute();
    }

    public static int loadMemberId(final SharedPreferences preferences) {
        final Integer memberId = new ResourceInitProtocol<Integer>(
                "Could not load own member id"
        ) {
            @Override
            protected Integer tryLoadFromStorage() throws Exception {
                if (preferences.contains("MemberId")) {
                    return preferences.getInt("MemberId", -1);
                }

                else {
                    return null;
                }
            }
        }.execute();

        return memberId;
    }

    public static boolean hadJoined(final SharedPreferences preferences) {
        return preferences.contains("MemberId");
    }

    public void upload(final PfoertnerService service, final Authentication auth, final MemberData data) {
        this.uploadMemberTask.whenDone(
                aVoid -> this.uploadMemberTask.execute(
                        service,
                        auth,
                        data
                )
        );
    }

    public MemberData toData() {
        return new MemberData(
                this.id,
                this.firstName,
                this.lastName,
                this.status
        );
    }

    public int getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final SharedPreferences settings, final String accessToken){
        this.accessToken = accessToken;
        Office.writeMembersToLocalStorage(settings,this.office.membersToData());
    }

    public void setLastName(final PfoertnerService service, final Authentication auth, final String newLastName) {
        final MemberData data = new MemberData(
                this.id,
                this.firstName,
                newLastName,
                this.status
        );

        upload(service, auth, data);
    }

    public void setFirstName(final PfoertnerService service, final Authentication auth, final String newFirstName) {
        final MemberData data = new MemberData(
                this.id,
                newFirstName,
                this.lastName,
                this.status
        );

        upload(service, auth, data);
    }

    public void setStatus(final PfoertnerService service, final Authentication auth, final String newStatus) {
        final MemberData data = new MemberData(
                this.id,
                this.firstName,
                this.lastName,
                newStatus
        );

        upload(service, auth, data);
    }

    public void updateAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        this.downloadMemberTask.whenDone(
                aVoid -> downloadMemberTask.execute(preferences, service, auth)
        );
    }

    void updateByData(final MemberData data) {
        final String oldFirstName = this.firstName;
        final String oldLastName = this.lastName;
        final String oldStatus = this.status;
        final String oldServerAuthCode = this.serverAuthCode;

        this.firstName = data.firstName;
        if (!oldFirstName.equals(data.firstName)) {
            this.notifyEachObserver(memberObserver -> memberObserver.onFirstNameChanged(this.firstName));
        }

        this.lastName = data.lastName;
        if (!oldLastName.equals(data.lastName)) {
            this.notifyEachObserver(memberObserver -> memberObserver.onLastNameChanged(this.lastName));
        }

        this.serverAuthCode = data.serverAuthCode;
        if(!oldServerAuthCode.equals(data.serverAuthCode)){
            this.notifyEachObserver(memberObserver -> memberObserver.onServerAuthCodeChanged(this.serverAuthCode));
        }

        this.status = data.status;
        if (
                data.status == null && oldStatus != null
             || data.status != null && oldStatus == null
             || data.status != null && oldStatus != null && !oldStatus.equals(data.status)
        ) {
            this.notifyEachObserver(memberObserver -> memberObserver.onStatusChanged(this.status));
        }
    }

    private class DownloadMemberTask extends RequestTask<MemberData> {
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
        public MemberData doRequests() throws Exception {
            final MemberData updatedMemberData = service
                    .loadMember(this.auth.id, Member.this.id)
                    .execute()
                    .body();

            return updatedMemberData;
        }

        @Override
        protected void onSuccess(final MemberData updatedMemberData) {
            // Office übernimmt momentan lokale Speicherung, sollte besser nach hier ausgelagert werden
            Office.writeMembersToLocalStorage(this.settings, Member.this.office.membersToData());

            Member.this.updateByData(updatedMemberData);
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to download new member data.");
        }
    }

    private class UploadMemberTask extends RequestTask<Void> {
        private PfoertnerService service;
        private Authentication auth;
        private MemberData data;

        public void execute(final PfoertnerService service, final Authentication auth, final MemberData data) {
            this.service = service;
            this.auth = auth;
            this.data = data;

            super.execute();
        }

        @Override
        public Void doRequests() throws Exception {
            service.updateMember(
                    this.auth.id,
                    this.data.id,
                    this.data
            ).execute();

            return null;
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to update member data on the server.");
        }
    }
}
