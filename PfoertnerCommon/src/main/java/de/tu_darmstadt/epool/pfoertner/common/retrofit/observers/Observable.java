package de.tu_darmstadt.epool.pfoertner.common.retrofit.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Observable<T> {
    private final List<T> observers = new ArrayList<>();

    public final void addObserver(final T observer) {
        this.observers.add(observer);
    }

    public final void deleteObserver(final T observer) {
        this.observers.remove(observer);
    }

    protected final void notifyEachObserver(final Consumer<T> notificationFunction) {
        this.observers.forEach(notificationFunction);
    }
}
