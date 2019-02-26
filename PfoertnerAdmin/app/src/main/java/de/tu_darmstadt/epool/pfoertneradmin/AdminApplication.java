package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.Context;
import android.content.Intent;

import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class AdminApplication extends PfoertnerApplication {
    private Optional<Integer> maybeMemberId = Optional.empty();

    @Override
    public void onInit() {
        if (Member.hadJoined(getSettings())) {
            maybeMemberId = Optional.of(Member.loadMemberId(
                    getSettings()
            ));
        }

        startService(new Intent(this,ProcessAppointmentRequest.class));
    }

    public void setMemberId(final int id) {
        checkInitStatus();

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
