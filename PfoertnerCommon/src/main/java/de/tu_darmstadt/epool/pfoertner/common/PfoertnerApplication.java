package de.tu_darmstadt.epool.pfoertner.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Optional;

import com.jakewharton.threetenabp.AndroidThreeTen;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class PfoertnerApplication extends Application {
    private SharedPreferences preferences;
    private Password password;
    private PfoertnerService service;
    private User device;
    private Authentication authentication;
    private Optional<Office> maybeOffice = Optional.empty();

    private boolean hadBeenInitialized = false;

    // Needs to be called in a RequestTask
    public void init() {
        if (hadBeenInitialized) {
            return;
        }

        this.password = Password.loadPassword(this.preferences);
        this.service = PfoertnerService.makeService();
        this.device = User.loadDevice(this.preferences, this.service, this.password);
        this.authentication = Authentication.authenticate(this.preferences, this.service, this.device, this.password, this);

        if (Office.hadBeenRegistered(this.preferences)) {
            this.maybeOffice = Optional.of(
                    Office.loadOffice(this.preferences, this.service, this.authentication)
            );
        }

        else {
            this.maybeOffice = Optional.empty();
        }

        this.hadBeenInitialized = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);

        this.preferences = getSharedPreferences(PREFERENCES_NAME,0);
    }

    private void checkInitStatus() {
        if (!hadBeenInitialized) {
            throw new IllegalStateException("The application has to be initialized before you can use most methods!");
        }
    }

    public static PfoertnerApplication get(final Context context) {
        return (PfoertnerApplication) context.getApplicationContext();
    }

    public SharedPreferences getSettings() {
        return this.preferences;
    }

    public Password getPassword() {
        checkInitStatus();

        return this.password;
    }

    public PfoertnerService getService() {
        checkInitStatus();

        return this.service;
    }

    public User getDevice() {
        checkInitStatus();

        return this.device;
    }

    public Authentication getAuthentication() {
        checkInitStatus();

        return this.authentication;
    }

    public Office getOffice() {
        checkInitStatus();

        try {
            return this.maybeOffice
                    .orElseThrow(() -> new RuntimeException("You can only retrieve an office object, after having it set at least once after app installation (registering or joining)."));
        }

        catch (final Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean hasOffice() {
        return this.maybeOffice.isPresent();
    }

    public void setOffice(final Office office) {
        checkInitStatus();

        this.maybeOffice = Optional.of(office);
    }
}
