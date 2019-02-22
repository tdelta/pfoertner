package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

public interface InitStatus {
    int getId();

    int joinedOfficeId();

    default boolean hasJoinedOffice() {
        return joinedOfficeId() >= 0;
    }
}
