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
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
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

    private final int id;
    private String lastName;
    private String firstName;
    private String pictureMD5;

    private final Office office;

    private final DownloadMemberTask downloadMemberTask = new DownloadMemberTask();
    private final UploadMemberTask uploadMemberTask = new UploadMemberTask();
    private final DownloadPictureTask downloadPictureTask = new DownloadPictureTask();
    private final UploadPictureTask uploadPictureTask = new UploadPictureTask();

    Member(final Office office, final MemberData data) {
        super();

        this.id = data.id;
        this.lastName = data.lastName;
        this.firstName = data.firstName;
        this.pictureMD5 = data.pictureMD5;
        this.office = office;
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
                this.pictureMD5
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

    public Optional<Bitmap> getPicture(final File filesDir) {
        try {
            final File f = new File(filesDir, Member.generatePicturePath(this.id));

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

    public void setLastName(final PfoertnerService service, final Authentication auth, final String newLastName) {
        final MemberData data = new MemberData(
                this.id,
                this.firstName,
                newLastName,
                this.pictureMD5
        );

        upload(service, auth, data);
    }

    public void setFirstName(final PfoertnerService service, final Authentication auth, final String newFirstName) {
        final MemberData data = new MemberData(
                this.id,
                newFirstName,
                this.lastName,
                this.pictureMD5
        );

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

    void updateByData(final MemberData data) {
        final String oldFirstName = Member.this.firstName;
        final String oldLastName = Member.this.lastName;
        final String oldPictureMD5 = Member.this.pictureMD5;

        Member.this.firstName = data.firstName;
        if (!oldFirstName.equals(data.firstName)) {
            Member.this.notifyEachObserver(memberObserver -> memberObserver.onFirstNameChanged(Member.this.firstName));
        }

        Member.this.lastName = data.lastName;
        if (!oldLastName.equals(data.lastName)) {
            Member.this.notifyEachObserver(memberObserver -> memberObserver.onLastNameChanged(Member.this.lastName));
        }

        Member.this.pictureMD5 = data.pictureMD5;
        if (
                oldPictureMD5 == null && data.pictureMD5 != null
                        || oldPictureMD5 != null && data.pictureMD5 == null
                        || oldPictureMD5 != null && data.pictureMD5 != null && !oldPictureMD5.equals(data.pictureMD5)
        ) {
            Member.this.notifyEachObserver(MemberObserver::onPictureChanged);
        }
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
        final String oldPictureMD5 = Member.this.pictureMD5;
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
                    .downloadPicture(Member.this.id)
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

                        // Office übernimmt momentan lokale Speicherung, sollte besser nach hier ausgelagert werden
                        Office.writeMembersToLocalStorage(this.settings, Member.this.office.membersToData());

                        Member.this.updateByData(updatedMemberData);

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
                    .loadMember(this.auth.id, Member.this.id)
                    .execute()
                    .body();

            return updatedMemberData;
        }

        @Override
        protected void onSuccess(final MemberData data) {
            if (!downloadPictureIfNecessary(this.settings, this.service, this.auth, data, filesDir)) {
                // Office übernimmt momentan lokale Speicherung, sollte besser nach hier ausgelagert werden
                Office.writeMembersToLocalStorage(this.settings, Member.this.office.membersToData());

                Member.this.updateByData(data);
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
                    .uploadPicture(description, body, Member.this.id)
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
