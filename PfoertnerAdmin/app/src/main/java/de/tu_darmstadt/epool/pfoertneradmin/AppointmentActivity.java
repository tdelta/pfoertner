package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.Helpers;

public class AppointmentActivity extends AppCompatActivity{

    private static final int CONNECT_GOOGLE_CALENDAR = 1408;
    private static final int CALENDAR_NAME_ID = View.generateViewId();
    private static final String TAG = "AppointmentActivity";

    private AdminApplication app;
    private ViewGroup root;

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        cancelNotifications();
        setContentView(R.layout.activity_appointment);

        app = AdminApplication.get(this);
        root = findViewById(R.id.appointment_layout);

        app
                .getRepo()
                .getMemberRepo()
                .getMember(app.getMemberId())
                .observe(this, this::reactToMemberChange);
    }

    private void cancelNotifications(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(notificationManager!=null) {
            notificationManager.cancelAll();
        }
    }

    private void buildUI(final Member member, boolean calendarCreated){
        final LinearLayout topCard = root.findViewById(R.id.top_card);
        // TODO: topCard seems sometimes not to exist

        topCard.removeAllViews();
        Log.d(TAG,"Server auth code: " + member.getServerAuthCode());

        final View newContent;
        if(member.getServerAuthCode() != null){
            final View calendarName = getLayoutInflater().inflate(R.layout.text_card, topCard, false);
            calendarName.setId(CALENDAR_NAME_ID);

            final TextView text = calendarName.findViewById(R.id.text);

            if(calendarCreated) {
                text.setText("To display your office hours at the door panel, please enter them into the calendar \"Office hours\"");
            } else {
                text.setText("Waiting for the door panel to authenticate ...");
            }

            newContent = calendarName;
        }

        else {
            final View signInView = getLayoutInflater().inflate(R.layout.sign_in_card, topCard, false);
            signInView.findViewById(R.id.google_signin_button).setOnClickListener((view) -> connectGoogleCalendar());

            newContent = signInView;
        }

        topCard.addView(newContent);

        // TODO appointments
        // final AppointmentRequestList appointments = (AppointmentRequestList) getSupportFragmentManager().findFragmentById(R.id.appointments);
        // appointments.showAppointmentRequests(member.getAppointmentRequests());
    }

    private void reactToMemberChange(final Member member) {
        if (member != null) {
            if (member.getCalendarId() != null) {
                Helpers.requestCalendarsSync(this, "TODO: EMail");

                buildUI(member, true);
            }

            else {
                Log.d(TAG,"Though a member is present, there is no calendar id set yet, so we cant show any appointments.");

                buildUI(member, false);
            }

            // TODO: Handle appointment requests
            //@Override
            //public void onAppointmentRequestsChanged(final List<AppointmentRequest> appointmentRequests){
            //    final AppointmentRequestList appointments = (AppointmentRequestList) getSupportFragmentManager().findFragmentById(R.id.appointments);
            //    appointments.showAppointmentRequests(appointmentRequests);
            //}
        }

        else {
            Log.d(TAG, "Member is not (yet) set, so we can not access calendar information or appointments.");
        }
    }

    public void connectGoogleCalendar(){
        Log.d(TAG, "Trying to connect google calendar...");

        final String serverClientId = getString(R.string.server_client_id);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        final Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,CONNECT_GOOGLE_CALENDAR);
    }

    @Override
    @SuppressWarnings("CheckResult")
    protected void onActivityResult(final int requestCode,final int resultCode,final Intent data){
        if(requestCode==CONNECT_GOOGLE_CALENDAR){
            Log.d(TAG, "Google calendar oauth connection task finished.");

            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                final String authCode = account.getServerAuthCode();
                final String email = account.getEmail();

                // Send the auth code to the server
                app
                        .getRepo()
                        .getMemberRepo()
                        .setServerAuthCode(app.getMemberId(), authCode, email)
                        .subscribe(
                                () -> Log.d(TAG, "Successfully set new server auth code."),
                                throwable -> Log.e(TAG, "Failed to set new server auth code", throwable)
                        );
            }

            catch (final Exception e) {
                Log.d(TAG,"could not sign in", e);
            }
        }
    }
}