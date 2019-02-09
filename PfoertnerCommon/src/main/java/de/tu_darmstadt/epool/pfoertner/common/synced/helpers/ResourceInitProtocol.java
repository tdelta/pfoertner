package de.tu_darmstadt.epool.pfoertner.common.synced.helpers;

public class ResourceInitProtocol<T> {
    private final String errorMsg;

    public ResourceInitProtocol() {
        this.errorMsg = null;
    }

    public ResourceInitProtocol(final String errorMsg) {
        this.errorMsg = errorMsg;
    }

    protected T tryLoadFromServer() throws Exception {
        return null;
    }

    protected T tryLoadFromStorage() throws Exception {
        return null;
    }

    protected void saveToStorage(final T data) { }

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
