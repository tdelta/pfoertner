package de.tu_darmstadt.epool.pfoertner.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.spion.Spion;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class SyncService extends Service {
    private static final String TAG = "SyncService";

    private EventChannel eventChannel;

    @Override
    public IBinder onBind(Intent intent) {
        // We do not support binding
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        eventChannel = new EventChannel(SyncService.this) {
            @Override
            protected void onEvent(final EventType e, final @Nullable String payload) {
                switch (e) {
                    case AdminJoined:
                        SyncService.this.updateMembers();
                        break;
                    case OfficeMemberUpdated:
                        SyncService.this.updateMember(payload);
                        break;
                    case CalendarCreated:
                        SyncService.this.updateMemberCalendar(payload);
                        break;
                    case OfficeDataUpdated:
                        SyncService.this.updateOfficeData();
                        break;
                    case takephoto:
                        Spion spion = new Spion(getApplicationContext());
                        spion.takePhoto();
                        break;
                }
            }
        };

        this.eventChannel.listen();

        return START_STICKY;
    }

    private void updateMembers() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (app.hasOffice()) {
            app.getOffice().updateMembersAsync(app.getSettings(), app.getService(), app.getAuthentication(), app.getFilesDir());
        }

        else {
            Log.e(TAG, "Tried to update members, but office is not initialized.");
        }
    }

    private void updateMemberCalendar(final @Nullable String payload) {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (app.hasOffice()) {
            if (payload == null) {
                Log.e(TAG, "Tried to update member, but there was no payload!");
            } else {
                try {
                    final Optional<Member> maybeMember = app.getOffice().getMemberById(
                            Integer.parseInt(payload)
                    );

                    maybeMember.ifPresent(Member::calendarUpdated);
                } catch (NumberFormatException e){
                    Log.e(TAG, "Tried to update member, but the payload was invalid.");
                }
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
        super.onDestroy();

        this.eventChannel.shutdown();
    }
}
