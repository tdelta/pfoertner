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

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
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

/**
 * Old office member class that can save and load office member data in local storage, send modified data to the server
 * and propagate updates in the office members to observers
 *
 * Use instead: {@link de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.MemberRepository}
 */
@Deprecated
public class Member extends Observable<MemberObserver> {
    private static final String TAG = "Member";

    private MemberData memberData;

    private final Office office;

    private final DownloadMemberTask downloadMemberTask = new DownloadMemberTask();
    private final UploadMemberTask uploadMemberTask = new UploadMemberTask();
    private final DownloadPictureTask downloadPictureTask = new DownloadPictureTask();
    private final UploadPictureTask uploadPictureTask = new UploadPictureTask();

    /**
     * @param office Office that this office member belongs to
     * @param data Data to fill this wrapper
     */
    Member(final Office office, final MemberData data) {
        super();

        this.memberData = data;
        this.office = office;
    }

    /**
     * Tries to request a new office member from the server by joining an office.
     * If the request is successful, the data is saved into local settings.
     *
     * @param officeId Office to join
     * @param joinCode Secret office join code, obtained from the panel or other office members
     * @param firstName Name of the office member to create
     * @param lastName Name of the office member to create
     * @param status Status of the office member to create
     * @param settings Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param authtoken Authentication to make server requests (PfoertnerApplication.getAuthentication())
     * @return Loaded Member data
     */
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

    /**
     * Tries to load the id of the officemember associated with the app (used in pfoertner admin)
     *
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @throws RuntimeException if the settings dont contain an office member id
     * @return Office member id
     */
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

    /**
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @return True if an office member was saved into settings
     */
    public static boolean hadJoined(final SharedPreferences preferences) {
        return preferences.contains("MemberId");
    }

    /**
     * Uploads given office member data in an io thread.
     *
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param data Member data to upload
     */
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

    /**
     * Loads the picture of an office member from local storage.
     *
     * @param filesDir Directory containing the picture
     * @return Optional containing a picture or empty if an error occured
     */
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

    /**
     * Uploads a new last name for the office member to the server.
     *
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param newLastName last name to upload
     */
    public void setLastName(final PfoertnerService service, final Authentication auth, final String newLastName) {

        final MemberData data = memberData.deepCopy();
        data.lastName = newLastName;

        upload(service, auth, data);
    }

    /**
     * Uploads a new first name for the office member to the server.
     *
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param newFirstName first name to upload
     */
    public void setFirstName(final PfoertnerService service, final Authentication auth, final String newFirstName) {
        final MemberData data = memberData.deepCopy();
        data.firstName = newFirstName;

        upload(service, auth, data);
    }

    /**
     * Uploads a new status for the office member to the server.
     *
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param newStatus status to upload
     */
    public void setStatus(final PfoertnerService service, final Authentication auth, final String newStatus) {
        final MemberData data = memberData.deepCopy();
        data.status = newStatus;

        upload(service, auth, data);
    }

    /**
     * Uploads a new picture for the office member to the server
     *
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param type Type of the image file to upload (com.squareup.okhttp3.MediaType)
     * @param path Path of the image file to upload
     */
    public void setPicture(final PfoertnerService service, final Authentication auth, final String type, final String path) {
        this.uploadPictureTask.whenDone(
                aVoid -> this.uploadPictureTask.execute(service, auth, type, path)
        );
    }

    /**
     * Downloads member data from the server in an io thread. If the download was successful the new data is written into local settings.
     * Observers of this class are notified, if the data changed.
     *
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server (PfoertnerApplication.getService())
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param filesDir Directory to save data like office member pictures
     */
    public void updateAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth, final File filesDir) {
        this.downloadMemberTask.whenDone(
                aVoid -> downloadMemberTask.execute(preferences, service, auth, filesDir)
        );
    }

    public List<AppointmentRequest> getAppointmentRequests(){
        return memberData.appointmentRequests;
    }

    /**
     * Sends an update to the server to either delete or accept a given appointment request
     *
     * @param service Retrofit instance to communicate with the server
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param appointmentRequestId Id of the appointment request to modify
     * @param accepted True if the appointment shall be accepted, false to delete
     */
    public void setAppointmentRequestAccepted(final PfoertnerService service, final Authentication auth, final int appointmentRequestId, final boolean accepted){
        AppointmentRequest newAppointmentRequest = memberData.appointmentRequests.stream().
                filter(appointmentRequest -> appointmentRequest.id == appointmentRequestId).findAny().get();
        if(accepted){
            newAppointmentRequest.accepted = true;
            new RequestTask<Void>() {
                @Override
                protected Void doRequests() throws IOException{
                    service.patchAppointment(auth.id,appointmentRequestId,newAppointmentRequest).execute();
                    return null;
                }
            }.execute();
        } else {
            new RequestTask<Void>() {
                @Override
                protected Void doRequests() throws IOException{
                    service.removeAppointment(auth.id, newAppointmentRequest.id).execute();
                    return null;
                }
            }.execute();
        }
    }

    /**
     * Writes the url to the google calendar of the office member into local settings
     *
     * @param settings Local settings (PfoertnerApplication.getSettings())
     * @param newCalendarId New calendar id to write
     */
    public void setCalendarId(final SharedPreferences settings, final String newCalendarId){
        this.memberData.calendarId = newCalendarId;
        Office.writeMembersToLocalStorage(settings,office.membersToData());
    }

    public String getCalendarId(){
        return memberData.calendarId;
    }

    /**
     * Compares the new data to the existing data. If there are changes in a field, call the corresponding handler for all observers.
     * Afterwards update the existing data.
     *
     * @param data New data
     */
    void updateByData(final MemberData data) {
        Log.d(TAG, "The member with id " + this.getId() + " is being updated. We will now check, which attributes changed.");
        final MemberData oldMember = Member.this.memberData;
        Member.this.memberData = data;

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

        if (didChange(oldMember.appointmentRequests, data.appointmentRequests)){
            Member.this.notifyEachObserver(memberObserver -> memberObserver.onAppointmentRequestsChanged(data.appointmentRequests));
        }
    }

    /**
     * Checks, if two variables are equal. Deals with null.
     *
     * @param oldState Variable one
     * @param newState Variable two
     * @param <T> Type of the variables.
     * @return True if the variables are not equal
     */
    private static <T> boolean didChange(final T oldState, final T newState) {
        return oldState == null && newState != null
            || oldState != null && newState == null
            || oldState != null && newState!= null && !oldState.equals(newState);
    }

    /**
     * Writes a new email address for the office member into local settings and updates the field.
     *
     * @param settings Local settings (PfoertnerApplication.getSettings())
     * @param email New email address to write
     */
    public void setEmail(final SharedPreferences settings, final String email){
        memberData.email = email;
        Office.writeMembersToLocalStorage(settings,office.membersToData());
    }

    public String getEmail(){
        return memberData.email;
    }

    /**
     * Generates the filenames used for office member pictures
     *
     * @param memberId Id of the office member that the picture belongs to
     * @return A filename
     */
    private static String generatePicturePath(final int memberId) {
        return String.valueOf(memberId + "-picture.jpg");
    }

    /**
     * Takes an updated member data, compares the new md5 hash of the picture to the saved one
     * and downloads the picture if there was a change.
     *
     * @param preferences Local settings (PfoertnerApplication.getSettings())
     * @param service Retrofit instance to communicate with the server
     * @param auth Authentication of the device to make server requests (PfoertnerApplication.getAuthentication())
     * @param updatedMemberData Updated member data
     * @param filesDir Directory to save the picture
     * @return True if a new picture was downloaded
     */
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

    /**
     * Downloads the picture of the office member from the server and saves it in a file, executed in an io thread.
     */
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

    /**
     * Downloads the office member data for this office member in an io thread.
     * If the download is successful, the resulting data is written into local settings and the observers are notified
     */
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

    /**
     * Uploads the data for this office member to the server in an io thread.
     */
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

    /**
     * Uploads a picture for this office member to the server in an io thread.
     */
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
