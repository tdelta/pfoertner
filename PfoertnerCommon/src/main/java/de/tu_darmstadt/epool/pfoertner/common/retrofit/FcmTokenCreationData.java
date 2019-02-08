package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

public class FcmTokenCreationData {
    @Expose public final String fcmToken;

    public FcmTokenCreationData(final String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
