package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

public interface InitStatus {
    /**
     * Primary key for database access
     */
    int getId();

    /**
     * Id of the office that this device has joined.
     */
    int joinedOfficeId();

    /**
     * True if the device has joined an office and saved an office id.
     */
    default boolean hasJoinedOffice() {
        return joinedOfficeId() >= 0;
    }
}
