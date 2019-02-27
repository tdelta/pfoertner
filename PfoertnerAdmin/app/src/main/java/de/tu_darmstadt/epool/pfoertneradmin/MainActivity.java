package de.tu_darmstadt.epool.pfoertneradmin;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.design.widget.NavigationView;
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
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.GlobalStatusFragment;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.MemberStatusFragment;
import de.tu_darmstadt.epool.pfoertneradmin.viewmodels.MemberProfileViewModel;
import de.tu_darmstadt.epool.pfoertneradmin.fragments.RoomFragment;

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
                            () -> this.onInitialized(),
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

                                // Add code here to update the UI based on the item selected
                                // For example, swap UI fragments here
                                switch (menuItem.getItemId()) {
                                    case R.id.addMember:
                                        this.gotoQRCodeAcitvity(navigationView);
                                        break;

                                    case R.id.editProfile:
                                        this.gotoPictureUploader(navigationView);
                                        break;

                                    case R.id.showAppointments:
                                        this.goToAppointmentActivity(navigationView);
                                        break;
                                    case R.id.spion:
                                        this.gotoSpionActivity(navigationView);
                                        break;
                                }

                                return true;
                            });

                    final View header = navigationView.getHeaderView(0);
                    header.setOnClickListener(this::gotoPictureUploader);
                });
    }

    private void onInitialized() {
        this.startService(
                new Intent(MainActivity.this, SyncService.class)
        );

        {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            final GlobalStatusFragment globalStatusFragment = new GlobalStatusFragment();
            fragmentTransaction.add(R.id.global_status_view, globalStatusFragment);

            final RoomFragment roomFragment = new RoomFragment();
            fragmentTransaction.add(R.id.room_card, roomFragment);

            final MemberStatusFragment memberStatusFragment = new MemberStatusFragment();
            fragmentTransaction.add(R.id.member_status_view, memberStatusFragment);

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

    public void gotoSpionActivity(View view){
        Intent intent = new Intent(this, SpionActivity.class);
        startActivity(intent);
    }

    public void gotoQRCodeAcitvity(View view) {
        Intent intent = new Intent(this, showQRCodeActivity.class);
        startActivity(intent);
    }

    public void goToAppointmentActivity(View view) {
        Intent intent = new Intent(this,AppointmentActivity.class);
        startActivity(intent);
    }

    public void gotoPictureUploader(View view){
        Intent intent = new Intent(this, PictureUpload.class);
        startActivity(intent);
    }
}
