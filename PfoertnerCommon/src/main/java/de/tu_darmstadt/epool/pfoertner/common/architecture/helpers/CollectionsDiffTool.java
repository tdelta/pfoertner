package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CollectionsDiffTool<T> {
    private Collection<T> before = null;

    private List<T> removed = new ArrayList<>(0);
    private List<T> inserted = new ArrayList<>(0);
    private List<BeforeAfter<T>> changed = new ArrayList<>(0);

    protected abstract boolean haveSameId(final T lhs, final T rhs);

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

    public List<T> getRemoved() {
        return removed;
    }

    public List<T> getInserted() {
        return inserted;
    }

    public List<BeforeAfter<T>> getChanged() {
        return changed;
    }
}
