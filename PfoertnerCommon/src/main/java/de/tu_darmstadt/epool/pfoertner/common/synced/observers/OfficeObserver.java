package de.tu_darmstadt.epool.pfoertner.common.synced.observers;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

/**
 * Old implementation used to propagate changes in synchronized data to the views.
 * Use instead: {@link de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.OfficeRepository}
 */
@Deprecated
public interface OfficeObserver {
    default void onStatusChanged(final String newStatus) {}
    default void onJoinCodeChanged(final String newJoinCode) {}
    default void onMembersChanged(final List<Member> newMembers, final List<Integer> removedMemberIds) { }
}
