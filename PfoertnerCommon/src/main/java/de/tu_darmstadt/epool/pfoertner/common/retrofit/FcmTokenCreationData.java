package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

/**
 * Body of a request to the server that informs the server about the devices current fcm token.
 * The token is used to address the device with a firebase notification.
 */
public class FcmTokenCreationData {
    @Expose public final String fcmToken;

    public FcmTokenCreationData(final String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
