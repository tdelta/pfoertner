package de.tu_darmstadt.epool.pfoertneradmin;

import android.arch.persistence.room.Room;
import android.content.Context;

import java.util.Optional;
import java.util.concurrent.Executors;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;

public class AdminApplication extends PfoertnerApplication {
    private Optional<Integer> maybeMemberId = Optional.empty();


    @Override
    public void onInit() {
        if (Member.hadJoined(getSettings())) {
            maybeMemberId = Optional.of(Member.loadMemberId(
                    getSettings()
            ));
        }
    }

    public void setMemberId(final int id) {
        checkInitStatus();

        this
                .getRepo()
                .getMemberRepo()
                .refreshMember(id);

        this.maybeMemberId = Optional.of(id);
    }

    public int getMemberId() {
        checkInitStatus();

        try {
            return this.maybeMemberId
                    .orElseThrow(() -> new RuntimeException("You can only retrieve a member id, after having it set at least once after app installation (joining)."));
        }

        catch (final Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean hasMemberId() {
        return this.maybeMemberId.isPresent();
    }

    public static AdminApplication get(final Context context) {
        return (AdminApplication) context.getApplicationContext();
    }
}
