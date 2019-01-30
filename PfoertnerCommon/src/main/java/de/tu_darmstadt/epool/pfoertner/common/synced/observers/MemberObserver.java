package de.tu_darmstadt.epool.pfoertner.common.synced.observers;

public interface MemberObserver {
    default void onFirstNameChanged(final String newFirstName) {}
    default void onLastNameChanged(final String newFirstName) {}
}
