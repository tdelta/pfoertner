package de.tu_darmstadt.epool.pfoertner.common.synced.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Observable<T> {
    private final List<T> observers = new ArrayList<>();
    private final List<T> newObserversBuffer = new ArrayList<>(0);
    private final List<T> removedObserversBuffer = new ArrayList<>(0);
    private boolean observerListLock = false;

    public final void addObserver(final T observer) {
        if (observerListLock) {
            this.newObserversBuffer.add(observer);
        }

        else {
            this.observers.add(observer);
        }
    }

    public final void deleteObserver(final T observer) {
        if (observerListLock) {
            this.removedObserversBuffer.add(observer);
        }

        else {
            this.observers.remove(observer);
        }
    }

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
