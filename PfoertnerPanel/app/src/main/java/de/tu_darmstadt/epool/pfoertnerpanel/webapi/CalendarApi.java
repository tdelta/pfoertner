package de.tu_darmstadt.epool.pfoertnerpanel.webapi;

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
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CalendarApi {
    private static final String TAG = "CalendarApi";

    private final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final List<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR_READONLY,
            "https://www.googleapis.com/auth/plus.me",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/calendar"
    );

    private static final String clientId = "626288801350-vk790l2a56u0m25p63q36asu4tv7gnsr.apps.googleusercontent.com";
    private static final String clientSecret = "wHAYULXTwsZWMQ827ITPIEVr";
    private static final String credentialsPath = "pfoertner-e43d0751b099.p12";

    public Single<String> getAccessToken(final String serverAuthCode) {
        return Single.fromCallable(
                () -> {
                    final GoogleTokenResponse tokenResponse =
                            new GoogleAuthorizationCodeTokenRequest(
                                    HTTP_TRANSPORT,
                                    JacksonFactory.getDefaultInstance(),
                                    "https://oauth2.googleapis.com/token",
                                    clientId,
                                    clientSecret,
                                    serverAuthCode,
                                    ""
                            )
                                    .setScopes(SCOPES)
                                    .execute();

                    return tokenResponse.getAccessToken();
                }
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Credential> getCredential(final String oauthToken) {
        return Single.fromCallable(
                () -> {
                    final Credential credential = new GoogleCredential.Builder()
                            .setTransport(HTTP_TRANSPORT)
                            .setJsonFactory(JacksonFactory.getDefaultInstance())
                            .setClientSecrets(
                                    clientId,
                                    clientSecret)
                            .build();

                    credential.setAccessToken(oauthToken);

                    return credential;
                }
        )
                .subscribeOn(Schedulers.io());
    }

    public Single<String> getCalendarId(final Credential credential) {
        return Single.fromCallable(
                () -> {

                    final Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), credential)
                            .setApplicationName("Pfoertner")
                            .build();

                    final List<CalendarListEntry> calendarList = service
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

                        Log.d(TAG, "Since there was no calendar \"Office hours\", we created a new one, and it has the id " + newCalendar.getId());

                        id = newCalendar.getId();
                    }

                    else {
                        Log.d(TAG, "There has already been an calendar with summary \"Office hours\" with id " + id + ", we will be (re)using that one.");
                    }

                    return id;
                }
        )
                .subscribeOn(Schedulers.io());
    }

    public Single<List<Event>> getEvents(final String calendarId, final Credential credentials, final DateTime start, final DateTime end) {
        return Single.fromCallable(
                () -> {
                    final Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), credentials)
                            .setApplicationName("Pfoertner")
                            .build();

                    Log.d(TAG, "About to download events for calendar " + calendarId);

                    final Events events = service.events().list(calendarId)
                            .setMaxResults(10)
                            .setTimeMin(start)
                            .setTimeMax(end)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();

                    Log.d(TAG, "Downloaded the following events: " + events.getItems().toString());


                    return events.getItems();
                }
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
