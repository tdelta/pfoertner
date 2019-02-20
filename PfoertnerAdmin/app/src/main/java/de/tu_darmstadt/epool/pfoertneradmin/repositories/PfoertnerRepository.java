package de.tu_darmstadt.epool.pfoertneradmin.repositories;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertneradmin.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertneradmin.db.entity.MemberEntity;
import de.tu_darmstadt.epool.pfoertneradmin.model.Member;
import de.tu_darmstadt.epool.pfoertneradmin.model.Office;
import de.tu_darmstadt.epool.pfoertneradmin.webapi.PfoertnerApi;
import retrofit2.Response;

@Singleton
public class PfoertnerRepository {
    private PfoertnerApi api;
    private Authentication auth;

    private AppDatabase db;
    private Executor executor;

    private EventChannel eventChannel;

    @Inject
    public PfoertnerRepository(final Context context, final PfoertnerApi api, final Authentication auth, final AppDatabase db, final Executor executor) {
        this.api = api;
        this.auth = auth;
        this.db = db;
        this.executor = executor;

        this.eventChannel = new EventChannel(context) {
            @Override
            protected void onEvent(EventType e, @Nullable String payload) {
                if (e.equals(EventType.OfficeMemberUpdated)) {
                    final int id = Integer.valueOf(payload);

                    refreshMember(id);
                }
            }
        };
        this.eventChannel.listen();
    }

    public LiveData<? extends Member> getMember(final int memberId) {
        refreshMember(memberId);
        // Returns a LiveData object directly from the database.
        return db.memberDao().load(memberId);
    }

    public LiveData<? extends Office> getOffice(final int officeId) {
        refreshOffice(officeId);
        // Returns a LiveData object directly from the database.
        return db.officeDao().load(officeId);
    }

    private void refreshMember(final int memberId) {
        // Runs in a background thread.
        executor.execute(() -> {
            // Refreshes the data.
            try {
                final Response<MemberEntity> response = api.getMember(auth.id, memberId).execute();

                // Updates the database. The LiveData object automatically
                // refreshes, so we don't need to do anything else here.
                db.memberDao().save(response.body());
            }

            catch (final Exception e) {
                // TODO
                e.printStackTrace();
            }
        });
    }

    private void refreshOffice(final int officeId) {
        // Runs in a background thread.
        executor.execute(() -> {
            // Refreshes the data.
            try {
                final Response<MemberEntity> response = api.getMember(auth.id, officeId).execute();

                // Updates the database. The LiveData object automatically
                // refreshes, so we don't need to do anything else here.
                db.memberDao().save(response.body());
            }

            catch (final IOException e) {
                // TODO
                e.printStackTrace();
            }
        });
    }
}
