package de.tu_darmstadt.epool.pfoertner.common.retrofit.observers;

public interface OfficeObserver {
    default void onStatusChanged(final String newStatus) {}
    default void onJoinCodeChanged(final String newJoinCode) {}
}
