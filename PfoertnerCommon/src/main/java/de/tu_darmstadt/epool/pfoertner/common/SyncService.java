package de.tu_darmstadt.epool.pfoertner.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
            protected void onEvent(EventType e) {
                switch (e) {
                    case AdminJoined:
                        SyncService.this.updateMembers();
                        break;
                    case OfficeDataUpdated:
                        SyncService.this.updateOfficeData();
                }
            }
        };

        this.eventChannel.listen();

        return START_STICKY;
    }

    private void updateMembers() {
        // TODO
    }

    private void updateOfficeData() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (app.hasOffice()) {
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
