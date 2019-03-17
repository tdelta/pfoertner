package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;

/**
 * Holds a previous and current state of an instance
 * @param <T> The type of the instances
 */
public class BeforeAfter<T> {
    private final T before;
    private final T after;

    public BeforeAfter(T before, T after) {
        this.before = before;
        this.after = after;
    }

    /**
     * @return The previous state
     */
    public T getBefore() {
        return before;
    }

    /**
     * @return The current state
     */
    public T getAfter() {
        return after;
    }
}
