package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.synced.observers.MemberObserver;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class CalendarApi implements MemberObserver {

    private static final String TAG = "Calendar";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    private final Context context;
    private final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private final Member member;


    public CalendarApi(final Member member,final Context context){
        this.context = context;
        this.member = member;
        member.addObserver(this);
    }

    @Override
    public void onServerAuthCodeChanged(String newServerAuthCode) {
        new RequestTask<String>(){
            @Override
            public String doRequests() throws IOException{
                return getAccessToken(newServerAuthCode);
            }

            @Override
            public void onException(Exception e){
                Log.d(TAG,"Could not get an Oauth token from the server");
                e.printStackTrace();
            }

            @Override
            public void onSuccess(String result){
                PfoertnerApplication app = PfoertnerApplication.get(CalendarApi.this.context);
                CalendarApi.this.member.setAccessToken(app.getSettings(),result);
            }
        }.execute();
    }

    public String getAccessToken(String serverAccessKey) throws IOException{
        GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(
                        HTTP_TRANSPORT,
                        JacksonFactory.getDefaultInstance(),
                        "https://oauth2.googleapis.com/token",
                        context.getResources().getString(R.string.clientId),
                        context.getResources().getString(R.string.clientSecret),
                        serverAccessKey,
                        "").execute();

        return tokenResponse.getAccessToken();
    }

    private PrivateKey loadKey(){
        AssetManager assetManager = context.getApplicationContext().getAssets();

        try {
            InputStream stream = assetManager.open(context.getResources().getString(R.string.credentialsPath));
            KeyStore p12 = KeyStore.getInstance("pkcs12");
            p12.load(stream, "notasecret".toCharArray());
            Enumeration e = p12.aliases();
            PrivateKey key = null;
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                key = (PrivateKey) p12.getKey(alias, "notasecret".toCharArray());
                if (key != null) {
                    return key;
                }
            }
        } catch (Exception e){
            Log.d(TAG,"Could not load the private key for the Calendar API");
            e.printStackTrace();
        }

        return null;
    }

    public List<Event> getEvents(String calendarId, DateTime start, DateTime end) throws IOException{
        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setClientSecrets(
                        context.getResources().getString(R.string.clientId),
                        context.getResources().getString(R.string.clientSecret))
                .setServiceAccountId("110420475534815932936")
                .setServiceAccountPrivateKeyId("e43d0751b099d3a3186c4477431a1ebf955780f5")
          //      .setServiceAccountPrivateKey(key)
                .setServiceAccountScopes(SCOPES)
                .build();
        //credential.setAccessToken(ACCESS_TOKEN);

        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Pfoertner")
                .build();

        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("llj7g9m22bmb9201l0rc3bk93o@group.calendar.google.com")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        Log.d("Calendar","Executed API access");
        Log.d("Calendar",events.getItems().toString());
        return events.getItems();
    }
}
