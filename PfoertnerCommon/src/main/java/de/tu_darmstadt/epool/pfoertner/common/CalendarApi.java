package de.tu_darmstadt.epool.pfoertner.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

/**
 * (Old class for accessing the Google Calendar API. Can be removed.)
 */
@Deprecated
public class CalendarApi implements MemberObserver {

    private static final String TAG = "CalendarApi";
    private static final List<String> SCOPES = Arrays.asList(new String[]{
            CalendarScopes.CALENDAR_READONLY,
            "https://www.googleapis.com/auth/plus.me",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/calendar"}
        );

    private static final String clientId = "626288801350-vk790l2a56u0m25p63q36asu4tv7gnsr.apps.googleusercontent.com";
    private static final String clientSecret = "wHAYULXTwsZWMQ827ITPIEVr";
    private static final String credentialsPath = "pfoertner-e43d0751b099.p12";

    private final Context context;
    private final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private final NotifyAdminsTask notifyAdminsTask = new NotifyAdminsTask();
    private final GetAccessTokenTask getAccessTokenTask = new GetAccessTokenTask();

    private final Member member;

    private static int test_counter = 0;


    private class NotifyAdminsTask  extends RequestTask<Void>{

        private int memberId;

        public void execute(int memberId){
            this.memberId = memberId;
            execute();
        }

        @Override
        public Void doRequests() throws IOException{
            PfoertnerApplication app = PfoertnerApplication.get(CalendarApi.this.context);
            app.getService().createdCalendar(app.getAuthentication().id,memberId);
            return null;
        }
    }

    public CalendarApi(final Member member, final Context context){
        test_counter ++;
        Log.d(TAG,"Calendar APIs: "+test_counter);
        this.context = context;
        this.member = member;

        Log.d(TAG, "Adding a calender api object to the member with id " + member.getId());
        member.addObserver(this);

        if(member.getAccessToken()!=null) {
            Log.d("Access token", member.getAccessToken());
        }
    }

    private RequestTask<String> getCalendarIdTask = new RequestTask<String>(){
        @Override
        public String doRequests() throws IOException{
            return getCalendarId();
        }

        @Override
        public void onException(Exception e){
            Log.d(TAG,"Could not load calendars of the office member");
            e.printStackTrace();
        }

        @Override
        public void onSuccess(String result){
            PfoertnerApplication app = PfoertnerApplication.get(context);
            member.setCalendarId(app.getSettings(),result);
            notifyAdminsTask.whenDone(
                    aVoid -> notifyAdminsTask.execute(member.getId())
            );
        }
    };

    private static boolean dirtyHack = false;

    private class GetAccessTokenTask extends RequestTask<String> {
        private String newServerAuthCode;

        void execute(final String newServerAuthCode) {
            this.newServerAuthCode = newServerAuthCode;

            super.execute();
        }

        @Override
        public String doRequests() throws IOException{
            Log.d(TAG,"Requesting an auth token from Google");

            // only get new token, if server auth code truly changed
            // TODO: Das hier mehrfach auszulösen bei gleichem Code sollte eig. nicht gehen?
            //if (newServerAuthCode != null && !newServerAuthCode.equals(CalendarApi.this.member.getServerAuthCode()) || CalendarApi.this.member.getAccessToken() == null) {
            if (!dirtyHack) {
                dirtyHack = true;

                return getAccessToken(newServerAuthCode);
            }

            else {
                return CalendarApi.this.member.getAccessToken();
            }
        }

        @Override
        public void onException(Exception e){
            Log.d(TAG,"Could not get an Oauth token from the server");
            Log.d(TAG,e.getMessage());
            Log.d(TAG,e.toString());
            e.printStackTrace();
        }

        @Override
        public void onSuccess(String result){
            Log.d(TAG, "Successfully retrieved new access token. Setting access token of member with id " + CalendarApi.this.member.getId() + " to " + result);

            final PfoertnerApplication app = PfoertnerApplication.get(CalendarApi.this.context);
            CalendarApi.this.member.setAccessToken(app.getSettings(),result);

            getCalendarIdTask.whenDone(
                    aVoid -> getCalendarIdTask.execute()
            );
        }
    };

    @Override
    public void onServerAuthCodeChanged(final String newServerAuthCode) {
        Log.d(TAG, "The server calendar api auth code changed, so we need to get a new access token from the client.");

        getAccessTokenTask.whenDone(
                aVoid -> getAccessTokenTask.execute(newServerAuthCode)
        );

        Log.d(TAG,newServerAuthCode);
    }

    private String getCalendarId() throws IOException{
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), getCredential())
                .setApplicationName("Pfoertner")
                .build();
        List<CalendarListEntry> calendarList = service
                .calendarList().list().execute().getItems();
        String id = null;
        for(CalendarListEntry entry: calendarList){
            if(entry.getSummary().equals("Office hours"))
                id = entry.getId();
        }
        if(id == null){
            com.google.api.services.calendar.model.Calendar newCalendar = new com.google.api.services.calendar.model.Calendar();
            newCalendar.setSummary("Office hours");
            newCalendar = service.calendars().insert(newCalendar).execute();
            Log.d(TAG,newCalendar.getId());
            id = newCalendar.getId();
        }
        return id;
    }

    public String getAccessToken(String serverAccessCode) throws IOException{
        GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(
                        HTTP_TRANSPORT,
                        GsonFactory.getDefaultInstance(),
                        "https://oauth2.googleapis.com/token",
                        clientId,
                        clientSecret,
                        serverAccessCode,
                        "")
                        .setScopes(SCOPES)
                        .execute();
        tokenResponse.getRefreshToken();
        return tokenResponse.getAccessToken();
    }

    private Credential getCredential(){
        if(member.getAccessToken() == null){
            throw new RuntimeException("Cannot access calendar data before authenticating");
        }

        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(
                        clientId,
                        clientSecret)
                .build();
        credential.setAccessToken(member.getAccessToken());
        return credential;
    }

    public List<Event> getEvents(DateTime start, DateTime end) throws IOException {
        if (this.member.getCalendarId() != null) {
            final String calendarId = this.member.getCalendarId();

            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), getCredential())
                    .setApplicationName("Pfoertner")
                    .build();

            Events events = service.events().list(calendarId)
                    .setMaxResults(10)
                    .setTimeMin(start)
                    .setTimeMax(end)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            Log.d(TAG, "Executed API access");
            Log.d(TAG, events.getItems().toString());

            return events.getItems();
        }

        else {
            throw new RuntimeException("There is no calendar id set, so we cant retrieve any events.");
        }
    }
}
