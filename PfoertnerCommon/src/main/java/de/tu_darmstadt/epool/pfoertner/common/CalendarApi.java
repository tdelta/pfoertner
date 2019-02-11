package de.tu_darmstadt.epool.pfoertner.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.internal.AuthAccountRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class CalendarApi implements MemberObserver {

    private static final String TAG = "Calendar";
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
    private final PrivateKey key;

    private final Member member;


    public CalendarApi(final Member member, final Context context){
        this.context = context;
        this.member = member;

        {
            final PfoertnerApplication app = PfoertnerApplication.get(context);

            key = app.getCalendarApiKey();
        }

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
            member.setCalendarId(app.getService(),app.getAuthentication(),result);
        }
    };

    @Override
    public void onServerAuthCodeChanged(String newServerAuthCode) {
        new RequestTask<String>(){
            @Override
            public String doRequests() throws IOException{
                Log.d(TAG,"Requesting an auth token from Google");
                return getAccessToken(newServerAuthCode);
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
                Log.d(TAG,result);
                PfoertnerApplication app = PfoertnerApplication.get(CalendarApi.this.context);
                CalendarApi.this.member.setAccessToken(app.getSettings(),result);
                getCalendarIdTask.execute();
            }
        }.execute();
        Log.d(TAG,newServerAuthCode);
    }

    private String getCalendarId() throws IOException{
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), getCredential())
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
                        JacksonFactory.getDefaultInstance(),
                        "https://oauth2.googleapis.com/token",
                        clientId,
                        clientSecret,
                        serverAccessCode,
                        "")
                        .setScopes(SCOPES)
                        .execute();
        return tokenResponse.getAccessToken();
    }

    private Credential getCredential(){
        if(member.getAccessToken() == null){
            throw new RuntimeException("Cannot access calendar data before authenticating");
        }
        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setClientSecrets(
                        clientId,
                        clientSecret)
                .build();
        credential.setAccessToken(member.getAccessToken());
        return credential;
    }

    public List<Event> getEvents(String calendarId, DateTime start, DateTime end) throws IOException{

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), getCredential())
                .setApplicationName("Pfoertner")
                .build();

        Events events = service.events().list(calendarId)
                .setMaxResults(10)
                .setTimeMin(start)
                .setTimeMax(end)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        Log.d(TAG,"Executed API access");
        Log.d(TAG,events.getItems().toString());
        return events.getItems();
    }
}
