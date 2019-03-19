package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper Class that holds the current state of a Collection and calculates differences from updates,
 * that can be used to execute database operations.
 * @param <T> The type of the Collection
 */
public abstract class CollectionsDiffTool<T> {
    private Collection<T> before = null;

    private List<T> removed = new ArrayList<>(0);
    private List<T> inserted = new ArrayList<>(0);
    private List<BeforeAfter<T>> changed = new ArrayList<>(0);

    /**
     * Has to be implemented for specific data types, to differenciate between an insert and an update.
     * @param lhs First instance
     * @param rhs Second instance
     * @return true if the instances refer to the same entry in the database.
     */
    protected abstract boolean haveSameId(final T lhs, final T rhs);

    /**
     * Compares the current state of the collection with an updated state and calculates the difference,
     * filling the removed, inserted and changed lists
     * @param update The state of the Collection after the update
     */
    public void insert(final Collection<T> update) {
        if (before == null) {
            removed = new ArrayList<>(0);
            inserted = new ArrayList<>(update);
            changed = new ArrayList<>(0);
        }

        else {
            // very inefficent...

            removed = before
                .stream()
                .filter(
                        element -> update.stream().noneMatch(other -> haveSameId(element, other))
                )
                .collect(Collectors.toList());

            inserted = update
                    .stream()
                    .filter(
                            element -> before.stream().noneMatch(other -> haveSameId(element, other))
                    )
                    .collect(Collectors.toList());

            final List<T> changedBefore = new ArrayList<>(before);
            changedBefore.removeAll(removed);

            changed = changedBefore
                    .stream()
                    .map(
                            element -> new BeforeAfter<>(
                                    element,
                                    update
                                            .stream()
                                            .filter(other -> haveSameId(element, other))
                                            .findAny()
                                            .get()
                            )
                    )
                    .collect(Collectors.toList());
        }

        before = update;
    }

    /**
     * @return The items that were removed the last time that insert was called
     */
    public List<T> getRemoved() {
        return removed;
    }

    /**
     * @return The items that were inserted the last time that insert was called
     */
    public List<T> getInserted() {
        return inserted;
    }

    /**
     * @return The items that were updated the last time that insert was called
     */
    public List<BeforeAfter<T>> getChanged() {
        return changed;
    }
}
