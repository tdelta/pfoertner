package de.tu_darmstadt.epool.pfoertner.common.synced.observers;

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public interface OfficeObserver {
    default void onStatusChanged(final String newStatus) {}
    default void onJoinCodeChanged(final String newJoinCode) {}
    default void onMembersChanged(final List<Member> newMembers, final List<Integer> removedMemberIds) { }
}
