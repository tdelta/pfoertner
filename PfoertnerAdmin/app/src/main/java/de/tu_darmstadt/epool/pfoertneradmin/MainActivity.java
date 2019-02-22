package de.tu_darmstadt.epool.pfoertneradmin;


import android.Manifest;
import android.accounts.AuthenticatorException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Optional;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import de.hdodenhof.circleimageview.CircleImageView;
import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Password;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.User;
import de.tu_darmstadt.epool.pfoertner.common.SyncService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.Office;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.Helpers;


public class MainActivity extends AppCompatActivity implements GlobalTextFragment.TextDialogListener, GlobalStatusFragment.StatusDialogListener {
    private static final String TAG = "PfoertnerAdmin_MainActivity";
    private final static int MY_PERMISSIONS_READ_CALENDAR = 1;
    private GlobalStatusFragment globalStatusMenu;
    private PersonalStatusFragment personalStatusMenu;

    private DrawerLayout mDrawerLayout;

    private void init() {
        final PfoertnerApplication app = PfoertnerApplication.get(this);

        if (!Office.hadBeenRegistered(app.getSettings())) {
            Intent intent = new Intent(this, InitActivity.class);
            this.startActivityForResult(intent, 0);
        } else {
            new RequestTask<Void>(){
                @Override
                protected Void doRequests(){
                    app.init();
                    return null;
                }

                @Override
                protected void onSuccess(Void result) {
                    MainActivity.this.onInitialized();
                }

                @Override
                protected void onException(Exception e){
                    ErrorInfoDialog.show(MainActivity.this, e.getMessage(), aVoid -> init());
                }
            }.execute();
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

        globalStatusMenu = GlobalStatusFragment.newInstance(this);
        personalStatusMenu = PersonalStatusFragment.newInstance(this);
        final AdminApplication app = AdminApplication.get(this);

        Log.d(TAG, "App has been initialized. We are member #" + String.valueOf(app.getMemberId()));

        final Optional<Member> maybeMember = app.getOffice().getMemberById(
                app.getMemberId()
        );

        if (maybeMember.isPresent()) {
            final Member member = maybeMember.get();

            final NavigationView navigationView = findViewById(R.id.nav_view);
            final View header = navigationView.getHeaderView(0);

            final TextView drawerName = (TextView) header.findViewById(R.id.drawerName);
            final CircleImageView drawerPic = (CircleImageView) header.findViewById(R.id.drawerPic);

            final PersonalStatusView personalStatusView = (PersonalStatusView)  findViewById(R.id.personalStatusView);

            drawerName.setText(
                    member.getFirstName() + " " + member.getLastName()
            );

            member
                    .getPicture(app.getFilesDir())
                    .ifPresent(drawerPic::setImageBitmap);

            member.addObserver(
                    new MemberObserver() {
                        @Override
                        public void onFirstNameChanged(final String newFirstName) {
                            drawerName.setText(
                                    member.getFirstName() + " " + member.getLastName()
                            );
                        }

                        @Override
                        public void onLastNameChanged(final String newFirstName) {
                            drawerName.setText(
                                    member.getFirstName() + " " + member.getLastName()
                            );
                        }

                        @Override
                        public void onPictureChanged() {
                            member.getPicture(app.getFilesDir())
                                    .ifPresent(drawerPic::setImageBitmap);
                        }

                        @Override
                        public void onStatusChanged(final String newStatus) {
                            personalStatusView.setStatus(newStatus);
                        }
                    }
            );
        }

        else {
            Log.e(TAG, "No member registered, although app is fully initialized.");
        }
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

    public void editGlobalInfo(View view){
        if (globalStatusMenu != null) {
            globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");
        }
    }


    public void editPersonalStatus(View view){
        if (personalStatusMenu != null) {
            personalStatusMenu.show(getSupportFragmentManager(), "personalStatusMenu");
        }
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

    @Override
    public void updateGlobalStatus(String text) {
        if (globalStatusMenu != null) {
            globalStatusMenu.updateStatus(text);
            globalStatusMenu.show(getSupportFragmentManager(), "globalStatusMenu");
        }
    }

    @Override
    public void updatePersonalStatus(String text){
        if (personalStatusMenu != null){
            personalStatusMenu.updateStatus(text);
            personalStatusMenu.show(getSupportFragmentManager(), "personalStatusMenu");
        }
    }


    @Override
    public void startGlobalTextInput() {
        GlobalTextFragment textBox = new GlobalTextFragment();
        textBox.show(getSupportFragmentManager(), "insertTextBox");
    }

    @Override
    public void startPersonalTextInput(){
        PersonalTextFragment textBox = new PersonalTextFragment();
        textBox.show(getSupportFragmentManager(), "insertTextBox");
    }
}
