package de.tu_darmstadt.epool.pfoertner.common.synced.helpers;

/**
 * @param <T> Type of the data to load and save
 */
public class ResourceInitProtocol<T> {
    private final String errorMsg;

    public ResourceInitProtocol() {
        this.errorMsg = null;
    }

    public ResourceInitProtocol(final String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * Should be overridden with an implementation that loads data from a server
     * @return Loaded data
     */
    protected T tryLoadFromServer() throws Exception {
        return null;
    }

    /**
     * Should be overridden with an implementation that loads data from local storage
     * @return Loaded data
     */
    protected T tryLoadFromStorage() throws Exception {
        return null;
    }

    /**
     * Should be overridden with an implementation that saves data to local storage
     * @param data Data to save
     */
    protected void saveToStorage(final T data) { }

    /**
     * Given a server load method, a store method and a save method,
     * executes a protocol to request the data from the server and save it,
     * or load it from local storage if loading from the server fails
     * @throws RuntimeException if both loading from the server and from local storage fail
     * @return Retrieved data
     */
    public final T execute() {
        T data;

        try {
            data = tryLoadFromServer();

            if (data != null) {
                saveToStorage(data);
            }
        } catch (final Exception e) {
            e.printStackTrace();

            data = null;
        }

        try {
            if (data == null) {
                data = tryLoadFromStorage();
            }
        } catch (final Exception e) {
            e.printStackTrace();

            data = null;
        }

        if (data == null) {
            throw new RuntimeException(
                    this.errorMsg == null ?
                              "Could not load resource. Neither from the server nor from local storage."
                            : this.errorMsg
            );
        }

        return data;
    }
}
