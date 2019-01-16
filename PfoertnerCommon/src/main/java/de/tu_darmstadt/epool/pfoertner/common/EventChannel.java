package de.tu_darmstadt.epool.pfoertner.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class EventChannel {
    private static String EVENT_TYPE_KEY = "event";
    public enum EventType {
        AdminJoined,
        OfficeDataUpdated
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

    public void send(final EventType eventType) {
        final Intent intent = new Intent("events");

        intent.putExtra(EVENT_TYPE_KEY, eventType.name());

        broadcaster.sendBroadcast(intent);
    }

    protected void onEvent(final EventType e) {

    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String eventTypeStr = intent.getStringExtra(EVENT_TYPE_KEY);

            try {
                final EventType eventType = EventType.valueOf(eventTypeStr);

                onEvent(eventType);
            }

            catch (final IllegalArgumentException e) {
                e.printStackTrace();

                //TODO better log
            }
        }
    };
}
