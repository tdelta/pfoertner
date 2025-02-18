package de.tu_darmstadt.epool.pfoertneradmin;


import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.Optional;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.AppointmentFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.MainScreenFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.PictureUploadFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.ShowQrCodeFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.SpionFragment;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.MemberProfileViewModel;

/**
 * First Activity which is run after the App has been started.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PfoertnerAdmin_MainActivity";
    private final static int MY_PERMISSIONS_READ_CALENDAR = 1;

    private DrawerLayout mDrawerLayout;

    private MemberProfileViewModel memberViewModel;

    /**
     * Called, when the app is started.
     * If no office id was saved yet, it opens the InitActivity to scan a QR Code
     * Otherwise calls PfoertnerApplication.init
     */
    private void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (!Office.hadBeenRegistered(app.getSettings())) {
            Intent intent = new Intent(this, InitActivity.class);
            this.startActivityForResult(intent, 0);
        } else {
            // TODO: Handle disposable
            app
                    .init()
                    .subscribe(
                            this::onInitialized,
                            throwable -> {
                                Log.e(TAG, "Could not initialize app. Will offer user to retry.", throwable);

                                ErrorInfoDialog.show(MainActivity.this, throwable.getMessage(), aVoid -> init(), false);
                            }
                    );
        }
    }

    /**
     * Reaction to pressing the back button
     *
     * - Closes the drawer, if it is open
     * - Goes back to the previously shown fragment otherwise
     */
    @Override
    public void onBackPressed() {
        mDrawerLayout = findViewById(R.id.drawer_layout);

        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        }

        else if(getFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Select the fragment that should be displayed, based on the selected menu item
     * @param menuItemId Resource id of the selected menu item
     * @return Fragment class
     */
    private Class<? extends Fragment> selectFragment(@IdRes int menuItemId) {
        if (menuItemId == R.id.showHome)
            return MainScreenFragment.class;
        if (menuItemId == R.id.addMember)
            return ShowQrCodeFragment.class;
        if (menuItemId == R.id.editProfile)
            return PictureUploadFragment.class;
        if (menuItemId == R.id.showAppointments)
            return AppointmentFragment.class;
        if (menuItemId == R.id.spion)
            return SpionFragment.class;

        return null;
    }

    /**
     * Initializes the drawer navigation to load different fragments when items are selected.
     */
    private void initNavigation() {
        final Optional<ActionBar> maybeActionBar = Optional.ofNullable(
                getSupportActionBar()
        );

        if (!maybeActionBar.isPresent()) {
            Log.e(TAG, "Could not retrieve actionbar!");
        }

        maybeActionBar
                .ifPresent(actionbar -> {
                    actionbar.setDisplayHomeAsUpEnabled(true);
                    actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

                    mDrawerLayout = findViewById(R.id.drawer_layout);

                    final NavigationView navigationView = findViewById(R.id.nav_view);
                    navigationView.setNavigationItemSelectedListener(
                            menuItem -> {
                                // set item as selected to persist highlight
                                menuItem.setChecked(true);
                                // close drawer when item is tapped
                                mDrawerLayout.closeDrawers();

                                final FragmentManager fragmentManager = getSupportFragmentManager();
                                final FragmentTransaction transaction = fragmentManager.beginTransaction();

                                // Add code here to update the UI based on the item selected
                                // For example, swap UI fragments here
                                Class<? extends Fragment> fragmentClass = selectFragment(menuItem.getItemId());

                                if (fragmentClass != null) {
                                    try {
                                        transaction
                                                .replace(R.id.flContent, (Fragment) fragmentClass.newInstance())
                                                .addToBackStack(null)
                                                .commit();
                                    }

                                    catch (final Exception e) {
                                        Log.e(TAG, "Could instantiate fragment.", e);
                                    }
                                }

                                else {
                                    Log.e(TAG, "Cant handle unknown menu item.");
                                }

                                return true;
                            });

                    final View header = navigationView.getHeaderView(0);
                    header.setOnClickListener(
                            v -> gotoPictureUploader(navigationView, v)
                    );
                });
    }

    /**
     * Called after PfoertnerApplication init is done and we joined an office.
     * Fills the fragments with data about the office member
     */
    private void onInitialized() {
        {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.flContent, new MainScreenFragment());

            fragmentTransaction.commit();
        }

        final AdminApplication app = AdminApplication.get(this);

        Log.d(TAG, "App has been initialized. We are member #" + String.valueOf(app.getMemberId()));

        memberViewModel = new ViewModelProvider(this).get(MemberProfileViewModel.class);
        memberViewModel.init(app.getMemberId());

        memberViewModel.getMember().observe(this, member -> {
            if (member != null) {
                final NavigationView navigationView = findViewById(R.id.nav_view);
                final View header = navigationView.getHeaderView(0);

                final TextView drawerName = (TextView) header.findViewById(R.id.drawerName);

                Glide
                        .with(MainActivity.this)
                        .load(member.getPicture())
                        .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_white_120dp))
                        .signature(new ObjectKey(member.getPictureMD5() == null ? "null" : member.getPictureMD5()))
                        .into((CircleImageView) header.findViewById(R.id.drawerPic));

                drawerName.setText(
                        member.getFirstName() + " " + member.getLastName()
                );
            }

            else {
                Log.e(TAG, "Konnte MemberUI noch nicht aktualisieren, da Member (noch) nicht gesetzt ist, oder entfernt wurde!");
            }
        });
   }

    /**
     * Android lifecycle method, called when the views are created.
     * Builds the Navbar.
     * onBackStackChangedListener selects the correct item in the navbar when the back button is pressed.
     * calls init
     * @param savedInstanceState Saved state for recovering activity from background, not used here
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.getSupportFragmentManager().addOnBackStackChangedListener(
                () -> {
                    final Fragment current = getSupportFragmentManager().findFragmentById(R.id.flContent);
                    final NavigationView navigationView = findViewById(R.id.nav_view);

                    if (current != null && navigationView != null) {
                        Integer itemToCheck = null;

                        if (current instanceof MainScreenFragment) {
                            itemToCheck = R.id.showHome;
                        }

                        else if (current instanceof ShowQrCodeFragment) {
                            itemToCheck = R.id.addMember;
                        }

                        else if (current instanceof PictureUploadFragment) {
                            itemToCheck = R.id.editProfile;
                        }

                        else if (current instanceof AppointmentFragment) {
                            itemToCheck = R.id.showAppointments;
                        }

                        else if (current instanceof SpionFragment) {
                            itemToCheck = R.id.spion;
                        }

                        if (itemToCheck != null) {
                            navigationView.setCheckedItem(itemToCheck);
                        }
                    }
                }
        );

        initNavigation();

        init();
    }

    /**
     * Android callback to handle intents. Handles intent to open the appointment fragment
     * @param intent Received intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent has been called.");

        super.onNewIntent(intent);

        final String intentPurpose = intent.getExtras().getString("intentPurpose", null);
        if (intentPurpose != null) {
            if (intentPurpose.equals("AppointmentRequest")) {
                gotoAppointments();
            }
        }
    }

    /**
     * Callback for when a user clicks an item in the app bar
     * @param item Item that was clicked
     * @return True if menu processing is done here, returns the result of the method from the superclass
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Android callback, called when an intent started for result is finished
     * @param requestCode RequestCode of the intent
     * @param resultCode Returned code
     * @param data Returned data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult has been called.");

        super.onActivityResult(requestCode, resultCode, data);

        initNavigation(); // Ich weiß nicht warum, aber wenn man dies nicht hier auch setzt, dann lädt der Drawer manchmal nicht

        if(requestCode == 0) {
            Log.d(TAG, "JoinOfficeActivity triggered onActivityResult.");
            onInitialized();
        }
    }

    /**
     * Called when edit profile is selected in the navbar
     * @param navigationView The navbar view
     * @param view Header of the navbar
     */
    public void gotoPictureUploader(final NavigationView navigationView, final View view){
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.flContent, new PictureUploadFragment())
                .addToBackStack(null)
                .commit();

        navigationView.setCheckedItem(R.id.editProfile);
        mDrawerLayout.closeDrawers();
    }

    public void gotoAppointments(){
        final FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.flContent, new AppointmentFragment())
                .addToBackStack(null)
                .commit();

        mDrawerLayout.closeDrawers();
    }
}
