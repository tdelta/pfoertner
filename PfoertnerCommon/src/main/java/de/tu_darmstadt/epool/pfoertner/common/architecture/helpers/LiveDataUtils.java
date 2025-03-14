package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;


import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import android.database.Observable;
import androidx.annotation.Nullable;

/**
 * Collection of utilities for manipulating Room LiveData.
 */
public class LiveDataUtils {
    /**
     * Helper function that delivers the first data to an observer and then unregisters it.
     * @param data LiveData to observe
     * @param owner LifecycleOwner used to observe the LiveData. When it is not currently running, the Observer will not be called
     * @param observer Processes the data
     * @param <T> Type of data contained in the LiveData
     */
    public static <T> void observeOnce(final LiveData<T> data, final LifecycleOwner owner, final Observer<T> observer) {
        final Observer<T> helperObserver = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T newData) {
                observer.onChanged(newData);

                data.removeObserver(this);
            }
        };

        data.observe(owner, helperObserver);
    }
}
