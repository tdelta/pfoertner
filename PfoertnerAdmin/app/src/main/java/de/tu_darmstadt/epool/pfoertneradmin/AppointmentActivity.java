package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.Authentication;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.PfoertnerService;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;

public class AppointmentActivity extends AppCompatActivity{

    private static final int CONNECT_GOOGLE_CALENDAR = 1408;
    private static final int CALENDAR_NAME_ID = View.generateViewId();
    private static final String TAG = "AppointmentActivity";

    private AdminApplication app;
    private ViewGroup root;
    private Member member;

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        app = AdminApplication.get(this);
        root = findViewById(R.id.layout);

        try {
            member = app.getOffice().getMemberById(app.getMemberId()).orElseThrow(() -> new RuntimeException("The app needs to be initialized first"));
        } catch (Throwable e){
            throw new RuntimeException(e.getMessage());
        }

        buildUI();
        member.addObserver(new MemberObserver() {
            @Override
            public void onCalendarIdChanged(String newCalendarId) {
                //buildUI();
            }
        });
    }

    private void buildUI(){
        View calendarNameCard = root.findViewById(CALENDAR_NAME_ID);
        if(calendarNameCard != null) {
            root.removeView(calendarNameCard);
        }
        View signInCard = root.findViewById(R.id.sign_in_card);
        if(signInCard != null) {
            root.removeView(signInCard);
        }

        if(member.getServerAuthCode() != null){
            final View calendarName = getLayoutInflater().inflate(R.layout.text_card,root);
            calendarName.setId(CALENDAR_NAME_ID);
            TextView text = calendarName.findViewById(R.id.text);
            if(member.getCalendarId() != null) {
                text.setText("To display your office hours at the door panel, please enter them into the calendar \"Office hours\"");
            } else {
                text.setText("Waiting for the door panel to authenticate ...");
            }

        } else {
            final View signInView = getLayoutInflater().inflate(R.layout.sign_in_card,root);
            signInView.findViewById(R.id.google_signin_button).setOnClickListener(signInListener);
        }
    }

    public void connectGoogleCalendar(){
        final String serverClientId = getString(R.string.server_client_id);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/admin.directory.resource.calendar.readonly"))
                .requestServerAuthCode(serverClientId)
                .build();
        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        final Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,CONNECT_GOOGLE_CALENDAR);
    }

    @Override
    protected void onActivityResult(final int requestCode,final int resultCode,final Intent data){
        if(requestCode==CONNECT_GOOGLE_CALENDAR){
            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                final String authCode = account.getServerAuthCode();
                Log.d(TAG,"Got server auth code: " + authCode);

                // Send the auth code to the server
                PfoertnerService service = app.getService();
                Authentication auth = app.getAuthentication();
                member.setServerAuthCode(service,auth,authCode);
                buildUI();

            } catch (final Exception e) {
                Log.d(TAG,"could not sign in");
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener signInListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            connectGoogleCalendar();
        }
    };
}