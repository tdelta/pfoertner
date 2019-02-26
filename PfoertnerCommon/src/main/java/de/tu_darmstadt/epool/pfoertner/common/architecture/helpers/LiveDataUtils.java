package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.database.Observable;
import android.support.annotation.Nullable;

public class LiveDataUtils {
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
