package de.tu_darmstadt.epool.pfoertner.common;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Optional;

import com.jakewharton.threetenabp.AndroidThreeTen;

import de.tu_darmstadt.epool.pfoertner.common.architecture.db.AppDatabase;
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
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.ReplaySubject;

import static de.tu_darmstadt.epool.pfoertner.common.Config.PREFERENCES_NAME;

public class PfoertnerApplication extends Application {
    private static final String TAG = "PfoertnerApplicationLog";

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

    private Completable initProcess = null;

    private Completable buildInitProcess() {
        return Single.fromCallable(
                () -> {
                    Log.d(TAG, "App init process is now starting.");

                    Log.d(TAG, "Retrieving password, building service, loading device and authenticating.");
                    this.password = Password.loadPassword(this.preferences);
                    this.service = PfoertnerService.makeService();
                    this.device = User.loadDevice(this.preferences, this.service, this.password);
                    this.authentication = Authentication.authenticate(this.preferences, this.service, this.device, this.password, this);
                    Log.d(TAG, "Successfully retrieved password, service, device info and authentication.");

                    db = Room.databaseBuilder(this, AppDatabase.class, "AppDatabase").build();
                    repo = new PfoertnerRepository(api, this.authentication, db);

                    return repo;
                }
        )
                .doOnError(
                        throwable -> Log.e(TAG, "Something failed during base app initialization.", throwable)
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
                                                        Log.d(TAG, "The has already joined an office, so we will load it.");

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
                                                                .refreshAllLocalData();

                                                        officeIdSubject
                                                                .onNext(initStatus.joinedOfficeId());
                                                    } else {
                                                        Log.d(TAG, "The app has never joined an office, so we will not load one.");
                                                        this.maybeOffice = Optional.empty();
                                                    }

                                                    Log.d(TAG, "Initializing subclasses.");
                                                    onInit();
                                                    Log.d(TAG, "Subclasses initialized.");

                                                    Log.d(TAG, "Completing initialization.");
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
                .ignoreElement()
                .doOnComplete(
                        () -> Log.d(TAG, "Successfully completed app initialization.")
                )
                .cache()
                .doOnComplete(
                        () -> Log.d(TAG, "Returned cached successful app initialization.")
                );
    }

    // Needs to be called in a RequestTask
    public synchronized Completable init() {
        if (initProcess == null) {
            Log.d(TAG, "Building the initialization process completable.");

            initProcess = buildInitProcess();
        }

        return initProcess.doOnError(
                throwable -> {
                    Log.e(TAG, "Initializing the app failed, so we will reset the process, such that completing it may be retried.");

                    initProcess = null;
                }
        );
    }

    protected void onInit() { }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);

        RxJavaPlugins.setErrorHandler(Throwable::printStackTrace);

        this.preferences = getSharedPreferences(PREFERENCES_NAME,0);

        observeInitialization()
                .subscribe(
                        () -> {
                            Log.d(TAG, "Starting sync service on app initialization.");

                            startService(
                                    new Intent(this, SyncService.class)
                            );
                        },
                        throwable -> Log.e(TAG, "Failed to observe app initialization, this should never happen.")
                );
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

        return getRepo()
                .getOfficeRepo()
                .getOfficeOnce(office.getId())
                .doOnSuccess(
                        dboffice -> Log.d(TAG, "Office is now stored in db.")
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Storing office into db failed.")
                )
                .flatMapCompletable(
                        dboffice -> getRepo()
                            .getInitStatusRepo()
                            .setJoinedOfficeId(office.getId())
                )
                .andThen(
                        Completable.fromAction(
                                () -> officeIdSubject
                                        .onNext(office.getId())
                        )
                )
                .doOnComplete(
                        () -> Log.d(TAG, "Successfully set office.")
                )
                .doOnError(
                        throwable -> Log.e(TAG, "Failed to set office.", throwable)
                );
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
