package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

public interface Device {
    /**
     * Unique id
     */
    int getId();

    /**
     * Firebase token used by the server to address this device with notifications
     */
    String getFcmToken();
}
