package de.tu_darmstadt.epool.pfoertner.common.synced.observers;

public interface OfficeObserver {
    default void onStatusChanged(final String newStatus) {}
    default void onJoinCodeChanged(final String newJoinCode) {}
    default void onMembersChanged() { }
}
