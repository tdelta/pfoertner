package de.tu_darmstadt.epool.pfoertneradmin.di;

import android.content.Context;

import dagger.Component;

@Component(modules = {ViewModelModule.class, AppModule.class})
public interface MainComponent {
    void inject(final Context context);
}
