package de.tu_darmstadt.epool.pfoertneradmin.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCode;
import de.tu_darmstadt.epool.pfoertner.common.qrcode.QRCodeData;
import de.tu_darmstadt.epool.pfoertneradmin.AdminApplication;
import de.tu_darmstadt.epool.pfoertneradmin.AppointmentRequestList;
import de.tu_darmstadt.epool.pfoertneradmin.R;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.Helpers;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AppointmentFragment extends Fragment {
    private static final String TAG = "AppointmentFragment";

    private static final int CONNECT_GOOGLE_CALENDAR = 1408;
    private static final int CALENDAR_NAME_ID = View.generateViewId();

    public AppointmentFragment() {

    }

    /**
     *
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cancelNotifications();
    }

    /**
     *
     * @param inflater needed to create views in the fragment
     * @param container parent view of the fragment
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     * @return view for layout context
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_appointment, container, false);

        final AdminApplication app = AdminApplication.get(getContext());

        app.observeOfficeId()
                .subscribe(
                        id -> {
                            app
                                    .getRepo()
                                    .getMemberRepo()
                                    .getMember(app.getMemberId())
                                    .observe(this, member -> reactToMemberChange(mainView.findViewById(R.id.appointment_layout), member));

                            final AppointmentRequestList appointments = (AppointmentRequestList) getChildFragmentManager().findFragmentById(R.id.appointments);
                            app
                                    .getRepo()
                                    .getAppointmentRepository()
                                    .getAppointmentsOfMember(app.getMemberId())
                                    .observe(this, appointmentData -> appointments.showAppointmentRequests(appointmentData));

                        },
                        throwable -> Log.e(TAG,"Office could not be loaded", throwable)
                );

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void cancelNotifications(){
        final NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);

        if(notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    /**
     * This method displays the necessary cards to initially
     * setup the google calendar access.
     *
     *
     * @param root root view
     * @param member for which the calendar should be initialized
     * @param calendarCreated boolean whether the calender was setup already
     */
    private void buildUI(final View root, final Member member, boolean calendarCreated){
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
    }

    /**
     *
     * This method is called if the member has changed. Then there will
     * be a request to synchronise the local google calender with the
     * online google calendar.
     *
     * @param root view
     * @param member which has changed
     */
    private void reactToMemberChange(final View root, final Member member) {
        if (member != null) {
            if (member.getCalendarId() != null) {
                Helpers.requestCalendarsSync(getContext(), member.getEmail()!=null ? member.getEmail() : "TODO: Email");

                buildUI(root, member, true);
            }

            else {
                Log.d(TAG,"Though a member is present, there is no calendar id set yet, so we cant show any appointments.");

                buildUI(root, member, false);
            }
        }

        else {
            Log.d(TAG, "Member is not (yet) set, so we can not access calendar information or appointments.");
        }
    }

    /**
     *
     * This method establishes the connection to the
     * online google calender.
     *
     */
    public void connectGoogleCalendar(){
        Log.d(TAG, "Trying to connect google calendar...");

        final String serverClientId = getString(R.string.server_client_id);
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
                .requestServerAuthCode(serverClientId,true)
                .requestEmail()
                .build();
        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        final Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,CONNECT_GOOGLE_CALENDAR);
    }

    @Override
    @SuppressWarnings("CheckResult")
    public void onActivityResult(final int requestCode,final int resultCode,final Intent data){
        if(requestCode==CONNECT_GOOGLE_CALENDAR){
            Log.d(TAG, "Google calendar oauth connection task finished.");

            final Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                final GoogleSignInAccount account = task.getResult(ApiException.class);
                final String authCode = account.getServerAuthCode();
                final String email = account.getEmail();

                // Send the auth code to the server
                final AdminApplication app = AdminApplication.get(getContext());

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