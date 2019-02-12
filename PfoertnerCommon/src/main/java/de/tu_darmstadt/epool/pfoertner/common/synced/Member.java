package de.tu_darmstadt.epool.pfoertner.common.synced;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.CalendarApi;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.MemberData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeJoinData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.helpers.ResourceInitProtocol;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class Member extends Observable<MemberObserver> {
    private static final String TAG = "Member";

    private MemberData memberData;

    private final Office office;

    private CalendarApi calendarApi;

    private final DownloadMemberTask downloadMemberTask = new DownloadMemberTask();
    private final UploadMemberTask uploadMemberTask = new UploadMemberTask();
    private final DownloadPictureTask downloadPictureTask = new DownloadPictureTask();
    private final UploadPictureTask uploadPictureTask = new UploadPictureTask();

    Member(final Office office, final MemberData data) {
        super();

        this.memberData = data;
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

    public MemberData getMemberData(){
        return memberData;
    }

    public int getId() {
        return memberData.id;
    }

    public String getLastName() {
        return memberData.lastName;
    }

    public String getFirstName() {
        return memberData.firstName;
    }

    public Optional<Bitmap> getPicture(final File filesDir) {
        try {
            final File f = new File(filesDir, Member.generatePicturePath(memberData.id));

            final Bitmap bitmap = BitmapFactory.decodeStream(
                    new FileInputStream(f)
            );

            return Optional.ofNullable(bitmap);
        }

        catch (final IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not load officemember picture, there was an error while reading the file.");

            return Optional.empty();
        }
    }

    public String getServerAuthCode(){
        return memberData.serverAuthCode;
    }

    public String getAccessToken(){
        return memberData.oauthToken;
    }

    public void setCalendarApi(CalendarApi calendarApi){
        this.calendarApi = calendarApi;
    }

    public CalendarApi getCalendarApi() {
        return calendarApi;
    }

    public void setAccessToken(final SharedPreferences settings, final String accessToken){
        Log.d(TAG,"The access token of the member with id " + this.getId() + " is being set to " + accessToken +  ". Meanwhile, the server auth code is " + this.getServerAuthCode());

        memberData.oauthToken = accessToken;
        Office.writeMembersToLocalStorage(settings, office.membersToData());
    }

    public void setServerAuthCode(final PfoertnerService service, final Authentication auth, final String serverAuthCode){
        final MemberData data = memberData.deepCopy();
        data.serverAuthCode = serverAuthCode;

        upload(service,auth,data);
    }

    public void setLastName(final PfoertnerService service, final Authentication auth, final String newLastName) {

        final MemberData data = memberData.deepCopy();
        data.lastName = newLastName;

        upload(service, auth, data);
    }

    public void setFirstName(final PfoertnerService service, final Authentication auth, final String newFirstName) {
        final MemberData data = memberData.deepCopy();
        data.firstName = newFirstName;

        upload(service, auth, data);
    }

    public void setStatus(final PfoertnerService service, final Authentication auth, final String newStatus) {
        final MemberData data = memberData.deepCopy();
        data.status = newStatus;

        upload(service, auth, data);
    }

    public void setPicture(final PfoertnerService service, final Authentication auth, final String type, final String path) {
        this.uploadPictureTask.whenDone(
                aVoid -> this.uploadPictureTask.execute(service, auth, type, path)
        );
    }

    public void updateAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir) {
        this.downloadMemberTask.whenDone(
                aVoid -> downloadMemberTask.execute(preferences, service, auth, filesDir)
        );
    }

    public List<AppointmentRequest> getAppointmentRequests(){
        return memberData.appointmentRequests;
    }

    public void acceptAppointmentRequest(final PfoertnerService service, final Authentication auth, final int appointmentRequestId){
        final MemberData data = memberData.deepCopy();
        data.appointmentRequests.get(appointmentRequestId).accepted = true;

        upload(service,auth,data);
    }

    public void setCalendarId(final SharedPreferences settings, final String newCalendarId){
        this.memberData.calendarId = newCalendarId;
        Office.writeMembersToLocalStorage(settings,office.membersToData());
    }

    public String getCalendarId(){
        return memberData.calendarId;
    }

    public void calendarUpdated(){
        notifyEachObserver(memberObserver -> memberObserver.onCalendarCreated());
    }

    void updateByData(final MemberData data) {
        Log.d(TAG, "The member with id " + this.getId() + " is being updated. We will now check, which attributes changed.");
        final MemberData oldMember = Member.this.memberData;
        Member.this.memberData = data;

        Log.d(TAG,"Old server auth code: "+oldMember.serverAuthCode);
        Log.d(TAG,"New server auth code: "+data.serverAuthCode);

        if (didChange(oldMember.firstName, data.firstName)) {
            Member.this.notifyEachObserver(memberObserver -> memberObserver.onFirstNameChanged(data.firstName));
        }

        if (didChange(oldMember.lastName, data.lastName)) {
            Member.this.notifyEachObserver(memberObserver -> memberObserver.onLastNameChanged(data.lastName));
        }

        if (didChange(oldMember.pictureMD5, data.pictureMD5)) {
            Member.this.notifyEachObserver(MemberObserver::onPictureChanged);
        }

        if (didChange(oldMember.status, data.status)) {
            Member.this.notifyEachObserver(memberObserver -> memberObserver.onStatusChanged(data.status));
        }

        if (didChange(oldMember.serverAuthCode, data.serverAuthCode)){
            Log.d(TAG, "The server auth code of member " + this.getId() + " changed from " + oldMember.serverAuthCode + " to " + data.serverAuthCode);

            Member.this.notifyEachObserver(memberObserver -> memberObserver.onServerAuthCodeChanged(data.serverAuthCode));
        }

        if (didChange(oldMember.appointmentRequests, data.appointmentRequests)){
            Member.this.notifyEachObserver(memberObserver -> memberObserver.onAppointmentRequestsChanged(data.appointmentRequests));
        }
    }

    private static <T> boolean didChange(final T oldState, final T newState) {
        return oldState == null && newState != null
            || oldState != null && newState == null
            || oldState != null && newState!= null && !oldState.equals(newState);
    }

    public void setEmail(final SharedPreferences settings, final String email){
        memberData.email = email;
        Office.writeMembersToLocalStorage(settings,office.membersToData());
    }

    public String getEmail(){
        return memberData.email;
    }

    private static String generatePicturePath(final int memberId) {
        return String.valueOf(memberId + "-picture.jpg");
    }

    boolean downloadPictureIfNecessary(
            final SharedPreferences preferences,
            final PfoertnerService service,
            final Authentication auth,
            final MemberData updatedMemberData,
            final File filesDir
    ) {
        final String oldPictureMD5 = memberData.pictureMD5;
        final File pictureFile = new File(filesDir, Member.generatePicturePath(updatedMemberData.id));

        if (
               oldPictureMD5 == null && updatedMemberData.pictureMD5 != null
            || oldPictureMD5 != null && updatedMemberData.pictureMD5 == null
            || oldPictureMD5 != null && updatedMemberData.pictureMD5 != null && !oldPictureMD5.equals(updatedMemberData.pictureMD5)
            || updatedMemberData.pictureMD5 != null && !updatedMemberData.pictureMD5.isEmpty() && !pictureFile.exists()
        ) {
            // picture got updated, we need to download it
            Member.this.downloadPictureTask.whenDone(
                    aVoid -> Member.this.downloadPictureTask.execute(preferences, service, auth, updatedMemberData, filesDir)
            );

            return true;
        }

        else {
            return false;
        }
    }

    private class DownloadPictureTask extends RequestTask<ResponseBody> {
        private SharedPreferences settings;
        private PfoertnerService service;
        private Authentication auth;
        private MemberData updatedMemberData;
        private File filesDir;

        public void execute(final SharedPreferences settings, final PfoertnerService service, final Authentication auth, final MemberData updatedMemberData, final File filesDir) {
            this.settings = settings;
            this.service = service;
            this.auth = auth;
            this.updatedMemberData = updatedMemberData;
            this.filesDir = filesDir;

            super.execute();
        }

        @Override
        public ResponseBody doRequests() throws Exception {
            final ResponseBody body = service
                    .downloadPicture(Member.this.memberData.id)
                    .execute()
                    .body();

            return body;
        }

        @Override
        protected void onSuccess(final ResponseBody body) {
            if (body != null) {
                InputStream in = null;
                OutputStream out = null;

                try {
                    final File f = new File(filesDir, Member.generatePicturePath(this.updatedMemberData.id));

                    final boolean filePrepared;
                    if (f.exists()) {
                        filePrepared = f.delete();
                    }

                    else {
                        filePrepared = f.createNewFile();
                    }

                    if (filePrepared) {
                        in = body.byteStream();
                        out = new FileOutputStream(f);

                        IOUtils.copy(in, out);

                        Member.this.updateByData(updatedMemberData);

                        // Office übernimmt momentan lokale Speicherung, sollte besser nach hier ausgelagert werden
                        Office.writeMembersToLocalStorage(this.settings, Member.this.office.membersToData());

                        // update again, if the picture changed while downloading it
                        Member.this.updateAsync(this.settings, this.service, this.auth, this.filesDir);
                    }

                    else {
                        Log.e(TAG, "Failed to write file for new picture. Therefore failed to update member.");
                    }
                }

                catch (java.io.IOException e) {
                    Log.e(TAG, "Failed to write file for new picture. Therefore failed to update member.");
                }

                finally {
                    try {
                        if (in != null) {
                            in.close();
                        }

                        if (out != null) {
                            out.close();
                        }
                    }

                    catch (final IOException e) {
                        Log.e(TAG, "Failed to close picture file output stream.");
                    }
                }
            }

            else {
                Log.e(TAG, "Failed to download updated picture. Therefore also failed to update member.");
            }
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to download new picture. Therefore also failed to update member.");
        }
    }

    private class DownloadMemberTask extends RequestTask<MemberData> {
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
        public MemberData doRequests() throws Exception {
            final MemberData updatedMemberData = service
                    .loadMember(this.auth.id, Member.this.memberData.id)
                    .execute()
                    .body();

            return updatedMemberData;
        }

        @Override
        protected void onSuccess(final MemberData data) {
            if (!downloadPictureIfNecessary(this.settings, this.service, this.auth, data, filesDir)) {
                Member.this.updateByData(data);

                // Office übernimmt momentan lokale Speicherung, sollte besser nach hier ausgelagert werden
                Office.writeMembersToLocalStorage(this.settings, Member.this.office.membersToData());
            }
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to download new member pictureUri.");
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
            Log.e(TAG, "Failed to update member pictureUri on the server.");
        }
    }

    private class UploadPictureTask extends RequestTask<Void> {
        private PfoertnerService service;
        private Authentication auth;
        private String type;
        private String path;

        public void execute(final PfoertnerService service, final Authentication auth, final String type, final String path) {
            this.service = service;
            this.auth = auth;
            this.type = type;
            this.path = path;

            super.execute();
        }

        @Override
        public Void doRequests() throws Exception {
            final File file = new File(this.path);

            // create RequestBody instance from file
            final RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse(this.type),
                            file
                    );

            // MultipartBody.Part is used to send also the actual file name
            final MultipartBody.Part body =
                    MultipartBody.Part.createFormData("picture", file.getName(), requestFile);

            // add another part within the multipart request
            final String descriptionString = "personal office member picture";
            final RequestBody description =
                    RequestBody.create(
                            okhttp3.MultipartBody.FORM, descriptionString);

            // finally, execute the request
            final ResponseBody response = service
                    .uploadPicture(description, body, Member.this.memberData.id)
                    .execute()
                    .body();

            if (response == null) {
                throw new RuntimeException("Uploading member picture failed!");
            }

            return null;
        }

        @Override
        protected void onException(Exception e) {
            // TODO: Retry?
            Log.e(TAG, "Failed to update member pictureUri on the server.");
        }
    }
}
