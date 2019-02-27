package de.tu_darmstadt.epool.pfoertner.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class EventChannel {
    private static final String TAG = "EventChannel";

    private static String EVENT_TYPE_KEY = "event";
    private static String EVENT_PAYLOAD_KEY = "payload";
    public enum EventType {
        AdminJoined,
        OfficeDataUpdated,
        OfficeMemberUpdated,
        CalendarCreated
    }

    private final Context context;
    private final LocalBroadcastManager broadcaster;
    private boolean listening = false;

    public EventChannel(final Context context) {
        this.context = context;
        broadcaster = LocalBroadcastManager.getInstance(context);
    }

    public void listen() {
        if (!listening) {
            LocalBroadcastManager.getInstance(context).registerReceiver(messageReceiver,
                    new IntentFilter("events")
            );

            listening = true;
        }
    }

    public void shutdown() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(messageReceiver);

        listening = false;
    }

    public void send(final EventType eventType, final @Nullable String payload) {
        final Intent intent = new Intent("events");

        intent.putExtra(EVENT_TYPE_KEY, eventType.name());
        intent.putExtra(EVENT_PAYLOAD_KEY, payload);

        broadcaster.sendBroadcast(intent);
    }

    protected void onEvent(final EventType e, final @Nullable String payload) {

    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String eventTypeStr = intent.getStringExtra(EVENT_TYPE_KEY);
            final String payload = intent.getStringExtra(EVENT_PAYLOAD_KEY);

            try {
                final EventType eventType = EventType.valueOf(eventTypeStr);

                onEvent(eventType, payload);
            }

            catch (final IllegalArgumentException e) {
                Log.e(TAG, "Could not translate message into event.", e);
            }
        }
    };
}
