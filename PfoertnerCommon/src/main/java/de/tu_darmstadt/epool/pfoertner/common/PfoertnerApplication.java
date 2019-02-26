package de.tu_darmstadt.epool.pfoertner.common;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Optional;
import java.util.concurrent.Executors;

import com.jakewharton.threetenabp.AndroidThreeTen;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
import de.tu_darmstadt.epool.pfoertner.common.architecture.db.entities.OfficeEntity;
import de.tu_darmstadt.epool.pfoertner.common.architecture.repositories.PfoertnerRepository;
import de.tu_darmstadt.epool.pfoertner.common.architecture.webapi.PfoertnerApi;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class PfoertnerApplication extends Application {
    private static final String TAG = "PfoertnerApplication";

    private CompletableSubject isInitializedSubject = CompletableSubject.create();
    private ReplaySubject<Integer> officeIdSubject = ReplaySubject.createWithSize(1);

    private SharedPreferences preferences;
    private Password password;
    private PfoertnerService service;
    private User device;
    private Authentication authentication;
    private Optional<Office> maybeOffice = Optional.empty();

    private PfoertnerApi api = PfoertnerApi.makeApi();
    private AppDatabase db;
    private PfoertnerRepository repo;

    private boolean hadBeenInitialized = false;

    // Needs to be called in a RequestTask
    public Completable init() {
        if (hadBeenInitialized) {
            return Completable.complete();
        }

        return Single.fromCallable(
                () -> {
                    this.password = Password.loadPassword(this.preferences);
                    this.service = PfoertnerService.makeService();
                    this.device = User.loadDevice(this.preferences, this.service, this.password);
                    this.authentication = Authentication.authenticate(this.preferences, this.service, this.device, this.password, this);

                    db = Room.databaseBuilder(this, AppDatabase.class, "AppDatabase").build();
                    repo = new PfoertnerRepository(api, this.authentication, db);

                    return repo;
                }
        )
        .flatMap(
                repo ->
                    repo
                            .getInitStatusRepo()
                            .getInitStatus()
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .doOnError(
                                    throwable -> Log.e(TAG, "Could not retrive init status, app cant be initialized.", throwable)
                            )
                            .doOnSuccess(
                                    initStatus -> {
                                        if (initStatus.hasJoinedOffice()) {
                                            this.maybeOffice = Optional.of(
                                                    Office.loadOffice(this.preferences, this.service, this.authentication, this.getFilesDir())
                                            );

                                            repo
                                                    .getOfficeRepo()
                                                    .refreshOffice(
                                                            initStatus.joinedOfficeId()
                                                    );

                                            repo
                                                    .getMemberRepo()
                                                    .refreshAllMembers();
                                        }

                                        else {
                                            this.maybeOffice = Optional.empty();
                                        }

                                        onInit();

                                        this.hadBeenInitialized = true;
                                        this.isInitializedSubject.onComplete();
                                    }
                            )
                            .doOnError(
                                    throwable -> Log.e(TAG, "Failed either while initializing the office, or while initializing subclasses.", throwable)
                            )
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .ignoreElement();
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

    public Completable setOffice(final Office office) {
        checkInitStatus();

        this.maybeOffice = Optional.of(office);


        getRepo()
                .getOfficeRepo()
                .refreshOffice(office.getId());

        officeIdSubject
                .onNext(office.getId());

        return getRepo()
                .getInitStatusRepo()
                .setJoinedOfficeId(office.getId())
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    public ReplaySubject<Integer> observeOfficeId() {
        return officeIdSubject;
    }

    public AppDatabase getDb() {
        checkInitStatus();

        return db;
    }

    public PfoertnerRepository getRepo() {
        checkInitStatus();

        return repo;
    }

    public PfoertnerApi getApi() {
        checkInitStatus();

        return this.api;
    }

    public CompletableSubject observeInitialization() {
        return this.isInitializedSubject;
    }
}
