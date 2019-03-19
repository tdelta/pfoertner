package de.tu_darmstadt.epool.pfoertner.common.synced.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implements logic to maintain a list of observers and traverse the list to call onChanged methods.
 * @param <T> Type of Observer, for example the MemberObserver interface
 */
public class Observable<T> {
    private final List<T> observers = new ArrayList<>();
    private final List<T> newObserversBuffer = new ArrayList<>(0);
    private final List<T> removedObserversBuffer = new ArrayList<>(0);
    private boolean observerListLock = false;

    /**
     * Adds an observer to the internal list.
     * If the list is currently traversed to notify observers, the new observer will be added afterwards.
     * @param observer Observer to add
     */
    public final void addObserver(final T observer) {
        if (observerListLock) {
            this.newObserversBuffer.add(observer);
        }

        else {
            this.observers.add(observer);
        }
    }

    /**
     * Removes an observer from the internal list.
     * If the list is currently traversed to notify observers, the observer will be removed afterwards.
     * @param observer Observer to remove
     */
    public final void deleteObserver(final T observer) {
        if (observerListLock) {
            this.removedObserversBuffer.add(observer);
        }

        else {
            this.observers.remove(observer);
        }
    }

    /**
     * Traverses the observers in the internal list and calls a function for each of them.
     * @param notificationFunction Function that takes an observer, is called with each observer
     * @throws IllegalStateException If the list is currently being traversed to notify observers
     */
    protected final void notifyEachObserver(final Consumer<T> notificationFunction) {
        if (observerListLock) {
            throw new IllegalStateException("You cant execute actions which cause observers to be updated, since observers are already being updated right now.");
        }

        observerListLock = true;
        this.observers.forEach(notificationFunction);

        this.observers.removeAll(this.removedObserversBuffer);
        this.removedObserversBuffer.clear();

        this.observers.addAll(this.newObserversBuffer);
        this.newObserversBuffer.clear();

        observerListLock = false;
    }
}
