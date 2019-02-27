package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;

public class BeforeAfter<T> {
    private final T before;
    private final T after;

    public BeforeAfter(T before, T after) {
        this.before = before;
        this.after = after;
    }

    public T getBefore() {
        return before;
    }

    public T getAfter() {
        return after;
    }
}
