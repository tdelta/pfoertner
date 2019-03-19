package de.tu_darmstadt.epool.pfoertner.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Wrapper for broadcast manager, used to communicate between Messaging Service and Activities.
 */
public class EventChannel {
    private static final String TAG = "EventChannel";

    private static String EVENT_TYPE_KEY = "event";
    private static String EVENT_PAYLOAD_KEY = "payload";
    public enum EventType {
        AdminJoined,
        OfficeDataUpdated,
        OfficeMemberUpdated,
        AppointmentsUpdated,
        takephoto,
        DeviceUpdated,
        CalendarUpdated
    }

    private final Context context;
    private final LocalBroadcastManager broadcaster;
    private boolean listening = false;

    /**
     * @param context Context to set up the LocalBroadcastManager
     */
    public EventChannel(final Context context) {
        this.context = context;
        broadcaster = LocalBroadcastManager.getInstance(context);
    }

    /**
     * Listen for events, call onEvent when an event is received.
     */
    public void listen() {
        if (!listening) {
            LocalBroadcastManager.getInstance(context).registerReceiver(messageReceiver,
                    new IntentFilter("events")
            );

            listening = true;
        }
    }

    /**
     * Stop listening for events.
     */
    public void shutdown() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(messageReceiver);

        listening = false;
    }

    /**
     * Send an event to every EventChannel instance.
     *
     * @param eventType Type of the event
     * @param payload Payload of the event
     */
    public void send(final EventType eventType, final @Nullable String payload) {
        final Intent intent = new Intent("events");

        intent.putExtra(EVENT_TYPE_KEY, eventType.name());
        intent.putExtra(EVENT_PAYLOAD_KEY, payload);

        broadcaster.sendBroadcast(intent);
    }

    /**
     * Should be implemented when using this class. Callback for received events.
     *
     * @param e Type of the received event
     * @param payload Payload of the received event
     */
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
