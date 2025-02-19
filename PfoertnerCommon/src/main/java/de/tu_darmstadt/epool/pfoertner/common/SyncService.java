package de.tu_darmstadt.epool.pfoertner.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Observable;
import java.util.Optional;
import java.util.concurrent.Callable;

import de.tu_darmstadt.epool.pfoertner.common.spion.Spion;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class SyncService extends Service {
    private static final String TAG = "SyncService";

    private EventChannel eventChannel;
    private CompositeDisposable disposables;

    @Override
    public IBinder onBind(Intent intent) {
        // We do not support binding
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting sync service.");

        if (disposables != null) {
            disposables.dispose();
        }

        disposables = new CompositeDisposable();

        eventChannel = new EventChannel(SyncService.this) {
            @Override
            protected void onEvent(final EventType e, final @Nullable String payload) {
                switch (e) {
                    case AdminJoined:
                        SyncService.this.updateMembers(payload);
                        break;
                    case OfficeMemberUpdated:
                        SyncService.this.updateMember(payload);
                        break;
                    case OfficeDataUpdated:
                        SyncService.this.updateOfficeData();
                        break;
                    case takephoto:
                        Log.d("SpionNew","The new takePhoto service is about to launch!");
                        startService(new Intent(getApplicationContext(), Spion.class));
                        break;
                    case AppointmentsUpdated:
                        SyncService.this.updateAppointmentsOfMember(payload);
                        break;
                    case DeviceUpdated:
                        SyncService.this.updateDevice(payload);
                        break;
                }
            }
        };

        this.eventChannel.listen();

        Log.d(TAG, "Will synchronize all local data, as soon as the app state is ready.");
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        disposables.add(
            app
                .observeInitialization()
                .subscribe(
                        () -> {
                            Log.d(TAG, "Starting synchronization of all local data.");

                            app
                                    .getRepo()
                                    .refreshAllLocalData();
                        },
                        throwable -> Log.e(TAG, "Failed to observe app initialization. This should never happen.")
                )
        );

        return START_STICKY;
    }

    private void updateMembers(final @Nullable String payload) {
        if (payload == null) {
            Log.e(TAG, "Tried to update members, but there was no payload!");
        }

        else {
            try {
                final int officeId = Integer.parseInt(payload);

                final PfoertnerApplication app = PfoertnerApplication.get(this);

                app
                        .getRepo()
                        .getMemberRepo()
                        .refreshAllMembersFromOffice(officeId);

                if (app.hasOffice()) {
                    app.getOffice().updateMembersAsync(app.getSettings(), app.getService(), app.getAuthentication(), app.getFilesDir());
                }

                else {
                    Log.e(TAG, "Tried to update members, but office is not initialized.");
                }
            } catch (NumberFormatException e){
                Log.e(TAG, "Tried to update members, but the payload was invalid.");
            }
        }
    }

    private void updateAppointmentsOfMember(final String payload){
        Log.d(TAG,"Update Appointments");
        if(payload == null){
            Log.e(TAG,"Tried to update appointments of an office member but there was no payload!");

        } else {
            final PfoertnerApplication app = PfoertnerApplication.get(this);
            try {
                int memberId = Integer.parseInt(payload);
                app.getRepo()
                        .getAppointmentRepository()
                        .refreshAllAppointmentsFromMember(memberId);
            } catch (NumberFormatException e){
                Log.e(TAG,"Tried to update appointments of an officemember but the payload was invalid",e);
            }
        }
    }

    private void updateDevice(final @Nullable String payload) {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (payload == null) {
            Log.e(TAG, "Tried to update device, but there was no payload!");
        }

        else {
            try {
                final int deviceId = Integer.parseInt(payload);

                Log.d(TAG, "Refreshing device " + deviceId);
                app.getRepo().getDeviceRepo().refreshDevice(
                        deviceId
                );
            }

            catch (final NumberFormatException e) {
                Log.e(TAG, "Tried to update member, but the payload was invalid.");
            }
        }
    }

    private void updateMember(final @Nullable String payload) {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (app.hasOffice()) {
            if (payload == null) {
                Log.e(TAG, "Tried to update member, but there was no payload!");
            }
            
            else {
                try {
                    final int memberId = Integer.parseInt(payload);
                    app.getRepo().getMemberRepo().refreshMember(
                            memberId
                    );

                    final Optional<Member> maybeMember = app.getOffice().getMemberById(
                            memberId
                    );

                    maybeMember.ifPresent(
                            member -> member.updateAsync(app.getSettings(), app.getService(), app.getAuthentication(), app.getFilesDir())
                    );

                    if (!maybeMember.isPresent()) {
                        Log.e(TAG, "Tried to update member, but there is no member with that id.");
                    }
                }

                catch (final NumberFormatException e) {
                    Log.e(TAG, "Tried to update member, but the payload was invalid.");
                }
            }
        }

        else {
            Log.e(TAG, "Tried to update a member, but office is not initialized.");
        }
    }

    private void updateOfficeData() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (app.hasOffice()) {
            app
                    .getRepo()
                    .getOfficeRepo()
                    .refreshOffice(app.getOffice().getId()); // TODO Server sollte ID schicken

            app.getOffice().updateAsync(
                    app.getSettings(),
                    app.getService(),
                    app.getAuthentication()
            );
        }

        else {
            Log.e(TAG, "Got event to update office, but to do that, there must a local office object already be present.");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SyncService is being shut down.");
        super.onDestroy();

        this.eventChannel.shutdown();

        Log.d(TAG, "Disposing observers.");
        this.disposables.dispose();
    }
}
