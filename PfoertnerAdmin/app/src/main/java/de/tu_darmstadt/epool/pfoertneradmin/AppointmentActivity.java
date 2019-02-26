package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.Helpers;

public class AppointmentActivity extends AppCompatActivity{

    private static final int CONNECT_GOOGLE_CALENDAR = 1408;
    private static final int CALENDAR_NAME_ID = View.generateViewId();
    private static final String TAG = "AppointmentActivity";

    private AdminApplication app;
    private ViewGroup root;
    private Member member;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        member.deleteObserver(memberObserver);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        cancelNotifications();
        setContentView(R.layout.activity_appointment);

        app = AdminApplication.get(this);
        root = findViewById(R.id.layout);

        try {
            member = app.getOffice().getMemberById(app.getMemberId()).orElseThrow(() -> new RuntimeException("The app needs to be initialized first"));
        } catch (Throwable e){
            throw new RuntimeException(e.getMessage());
        }

        if(member.getServerAuthCode() == null) {
            buildUI(false);
        } else {
            buildUI(true);
        }
        member.addObserver(memberObserver);
    }

    private void cancelNotifications(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(notificationManager!=null) {
            notificationManager.cancelAll();
        }
    }

    private void buildUI(boolean calendarCreated){
        LinearLayout topCard = root.findViewById(R.id.top_card);
        topCard.removeAllViews();
        Log.d(TAG,"Server auth code: "+member.getServerAuthCode());

        if(member.getServerAuthCode() != null){
            final View calendarName = getLayoutInflater().inflate(R.layout.text_card,topCard);
            calendarName.setId(CALENDAR_NAME_ID);
            TextView text = calendarName.findViewById(R.id.text);
            if(calendarCreated) {
                text.setText("To display your office hours at the door panel, please enter them into the calendar \"Office hours\"");
            } else {
                text.setText("Waiting for the door panel to authenticate ...");
            }

        } else {
            final View signInView = getLayoutInflater().inflate(R.layout.sign_in_card,topCard);
            signInView.findViewById(R.id.google_signin_button).setOnClickListener((view) -> connectGoogleCalendar());
        }
        final AppointmentRequestList appointments = (AppointmentRequestList) getSupportFragmentManager().findFragmentById(R.id.appointments);
        appointments.showAppointmentRequests(member.getAppointmentRequests());
    }

    MemberObserver memberObserver = new MemberObserver() {
        @Override
        public void onCalendarCreated() {
            Helpers.requestCalendarsSync(AppointmentActivity.this,member.getEmail());
            buildUI(true);
        }

        @Override
        public void onServerAuthCodeChanged(String newServerAuthCode){
            Log.d(TAG,"onServerAuthCodeChanged");
            buildUI(false);
        }

        @Override
        public void onAppointmentRequestsChanged(final List<AppointmentRequest> appointmentRequests){
            final AppointmentRequestList appointments = (AppointmentRequestList) getSupportFragmentManager().findFragmentById(R.id.appointments);
            appointments.showAppointmentRequests(appointmentRequests);
        }
    };

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
    protected void onActivityResult(final int requestCode,final int resultCode,final Intent data){
        if(requestCode==CONNECT_GOOGLE_CALENDAR){
            Log.d(TAG, "Google calendar oauth connection task finished.");

            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                final String authCode = account.getServerAuthCode();
                final String email = account.getEmail();

                // Send the auth code to the server
                PfoertnerService service = app.getService();
                Authentication auth = app.getAuthentication();
                SharedPreferences settings = app.getSettings();
                member.setServerAuthCode(service,auth,authCode);
                member.setEmail(settings,email);
            }

            catch (final Exception e) {
                Log.d(TAG,"could not sign in", e);
            }
        }
    }
}