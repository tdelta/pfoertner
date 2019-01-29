package de.tu_darmstadt.epool.pfoertner.common.synced;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.OfficeJoinData;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Person;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.observers.Observable;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.observers.OfficeObserver;

public class Office extends Observable<OfficeObserver> {
    private final static String TAG = "Office";

    private final int id;
    private String joinCode;
    private String status;

    public int getId() {
        return id;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public String getStatus() {
        return status;
    }

    private static void writeToLocalStorage(final SharedPreferences preferences, final OfficeData data) {
        final SharedPreferences.Editor e = preferences.edit();

        e.putInt("OfficeId", data.id);
        e.putString("OfficeJoinData", data.joinCode);
        e.putBoolean("OfficeStatusIsValid", data.status != null);

        if (data.status != null) {
            e.putString("OfficeStatus", data.status);
        }

        e.apply();
    }

    private static OfficeData loadFromLocalStorage(final SharedPreferences preferences) {
        return new OfficeData(
                preferences.getInt("OfficeId", -1),
                preferences.getString("OfficeJoinData", ""),
                preferences.getBoolean("OfficeStatusIsValid", false) ?
                          preferences.getString("OfficeStatus", null)
                        : null
        );
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
            writeToLocalStorage(this.settings, updatedOfficeData);

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

    public Office(final OfficeData data) {
        super();

        this.id = data.id;
        this.joinCode = data.joinCode;
    }

    public void updateAsync(final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        this.downloadOfficeTask.whenDone(
                aVoid -> downloadOfficeTask.execute(preferences, service, auth)
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
        OfficeData officeData;

        if (preferences.contains("OfficeId") /*officeData already registered*/) {
            officeData = loadFromLocalStorage(preferences);
        }

        else {
            // Create officeData
            try {
                officeData = service
                        .createOffice(auth.id)
                        .execute()
                        .body();

                if (officeData != null) {
                    writeToLocalStorage(preferences, officeData);
                }
            }

            catch (final IOException e) {
                e.printStackTrace();
                officeData = null;
                // the if below will handle further steps
            }
        }

        if (officeData == null) {
            throw new RuntimeException("Could not create a new officeData. Do you have an internet connection?");
        }

        return new Office(officeData);
    }

    public static Office loadOffice(final SharedPreferences deviceRegistrationInfo, final PfoertnerService service, final Authentication auth) {
        final int officeID = deviceRegistrationInfo.getInt("OfficeId", -1);
        if (officeID == -1){
            throw new RuntimeException("OfficeData could not be loaded. Invalid officeId was loaded.");
        }

        return Office.loadOffice(
                officeID,
                deviceRegistrationInfo,
                service,
                auth
        );
    }

    public static Office loadOffice(final int officeId, final SharedPreferences preferences, final PfoertnerService service, final Authentication auth) {
        OfficeData officeData;

        try {
            officeData = service
                    .loadOffice(auth.id, officeId)
                    .execute()
                    .body();

            if (officeData != null) {Log.d("DEBUG", "vor api call");
                writeToLocalStorage(preferences, officeData);
            }
        }

        catch (final IOException e) {
            e.printStackTrace();
            officeData = null;
            // the if below will handle further steps
        }

        if (officeData == null) {
            Log.d(TAG, "Had to load officeData from local storage since we could not connect.");

            officeData = loadFromLocalStorage(preferences);
        }

        return new Office(officeData);
    }

    public static Person joinOffice(final int officeId, final String joinCode, String firstName, String lastName, SharedPreferences settings, PfoertnerService service, Authentication authtoken)  {
        try{
            final Person person = service.joinOffice(
                    authtoken.id,
                    officeId,
                    new OfficeJoinData(
                            joinCode,
                            firstName,
                            lastName
                    )
            ).execute().body();

            // TODO save Person in preferences

            return person;
        }

        catch(final IOException e) {
            e.printStackTrace();

            throw new RuntimeException("Could not join office. Do you have an internet connection?");
        }
    }

    public static boolean hadBeenRegistered(final SharedPreferences settings) {
        return settings.contains("OfficeId");
    }
}

