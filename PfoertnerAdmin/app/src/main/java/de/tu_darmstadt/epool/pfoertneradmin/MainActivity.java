package de.tu_darmstadt.epool.pfoertneradmin;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.Optional;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.AppointmentFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.MainScreenFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.PictureUploadFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.ShowQrCodeFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.SpionFragment;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.MemberProfileViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PfoertnerAdmin_MainActivity";
    private final static int MY_PERMISSIONS_READ_CALENDAR = 1;

    private DrawerLayout mDrawerLayout;

    private MemberProfileViewModel memberViewModel;

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
                                Class fragmentClass = null;
                                switch (menuItem.getItemId()) {
                                    case R.id.showHome:
                                        fragmentClass = MainScreenFragment.class;
                                        break;

                                    case R.id.addMember:
                                        fragmentClass = ShowQrCodeFragment.class;
                                        break;

                                    case R.id.editProfile:
                                        fragmentClass = PictureUploadFragment.class;
                                        break;

                                    case R.id.showAppointments:
                                        fragmentClass = AppointmentFragment.class;
                                        break;
                                    case R.id.spion:
                                        fragmentClass = SpionFragment.class;
                                        break;
                                }

                                if (fragmentClass != null) {
                                    try {
                                        transaction
                                                .replace(R.id.flContent, (Fragment) fragmentClass.newInstance())
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

    private void onInitialized() {
        {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.flContent, new MainScreenFragment());

            fragmentTransaction.commit();
        }

        final AdminApplication app = AdminApplication.get(this);

        Log.d(TAG, "App has been initialized. We are member #" + String.valueOf(app.getMemberId()));

        memberViewModel = ViewModelProviders.of(this).get(MemberProfileViewModel.class);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initNavigation();

        init();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        initNavigation(); // Ich weiß nicht warum, aber wenn man dies nicht hier auch setzt, dann lädt der Drawer manchmal nicht

        if(requestCode == 0) {
            onInitialized();
        }
    }

    public void gotoPictureUploader(final NavigationView navigationView, final View view){
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, new PictureUploadFragment()).commit();

        navigationView.setCheckedItem(R.id.editProfile);
        mDrawerLayout.closeDrawers();
    }
}
