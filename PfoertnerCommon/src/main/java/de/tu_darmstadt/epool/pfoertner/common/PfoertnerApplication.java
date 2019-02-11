package de.tu_darmstadt.epool.pfoertner.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Enumeration;
import java.util.Optional;

import com.jakewharton.threetenabp.AndroidThreeTen;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class PfoertnerApplication extends Application {
    private static final String TAG = "PfoertnerApplication";

    private SharedPreferences preferences;
    private Password password;
    private PfoertnerService service;
    private User device;
    private Authentication authentication;
    private Optional<Office> maybeOffice = Optional.empty();
    private Optional<PrivateKey> calendarApiKey = Optional.empty();

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
                    Office.loadOffice(this.preferences, this.service, this.authentication, this.getFilesDir())
            );
        }

        else {
            this.maybeOffice = Optional.empty();
        }

        onInit();

        this.hadBeenInitialized = true;
    }

    public PrivateKey getCalendarApiKey() {
        if (this.calendarApiKey.isPresent()) {
            return this.calendarApiKey.get();
        }

        else {
            final PrivateKey key = loadCalendarApiKey();

            this.calendarApiKey = Optional.ofNullable(key);

            return key;
        }
    }

    private PrivateKey loadCalendarApiKey() {
        final String credentialsPath = "pfoertner-e43d0751b099.p12";

        final AssetManager assetManager = this.getApplicationContext().getAssets();

        try {
            // FIXME: There must be a better way to load the private key
            final InputStream stream = assetManager.open(credentialsPath);
            final KeyStore p12 = KeyStore.getInstance("pkcs12");

            p12.load(stream, "notasecret".toCharArray());

            final Enumeration e = p12.aliases();

            PrivateKey key;
            while (e.hasMoreElements()) {
                final String alias = (String) e.nextElement();
                key = (PrivateKey) p12.getKey(alias, "notasecret".toCharArray());

                if (key != null) {
                    return key;
                }
            }
        }

        catch (Exception e){
            Log.d(TAG,"Could not load the private key for the Calendar API");

            e.printStackTrace();
        }

        return null;
    }

    protected void onInit() { }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);

        this.preferences = getSharedPreferences(PREFERENCES_NAME,0);
    }

    protected void checkInitStatus() {
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
