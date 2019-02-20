package de.tu_darmstadt.epool.pfoertneradmin.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertneradmin.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertneradmin.webapi.PfoertnerApi;

@Module
public class AppModule {
    final Context context;
    final PfoertnerApi api;

    AppModule(final Context context) {
        this.context = context;
        this.api = PfoertnerApi.makeApi();
    }

    @Provides
    Authentication getAuth() {
        return AdminApplication.get(context).getAuthentication();
    }

    @Provides
    AppDatabase getDb() {
        return AdminApplication.get(context).getDb();
    }

    @Provides
    PfoertnerApi getApi() {
        return this.api;
    }
}
