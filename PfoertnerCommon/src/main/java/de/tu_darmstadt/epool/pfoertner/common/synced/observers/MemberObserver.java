package de.tu_darmstadt.epool.pfoertner.common.synced.observers;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;

/**
 * Old implementation used to propagate changes in synchronized data to the views.
 * Use instead: {@link de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.MemberRepository}
 */
@Deprecated
public interface MemberObserver {
    default void onFirstNameChanged(final String newFirstName) {}
    default void onLastNameChanged(final String newFirstName) {}
    default void onPictureChanged() {}
    default void onStatusChanged(final String newStatus){}
    default void onServerAuthCodeChanged(final String serverAuthCode){}
    default void onAppointmentRequestsChanged(final List<AppointmentRequest> newAppointmentRequests){}
}
