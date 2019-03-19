package de.tu_darmstadt.epool.pfoertnerpanel.webapi;

import android.content.Context;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.EventChannel;
import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Uses the google web api to access the google calendar
 */
public class CalendarApi {
    private static final String TAG = "CalendarApi";

    private final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private EventChannel calendarUpdatesChannel;
    private PublishSubject<String> calendarUpdates = PublishSubject.create();

    private static final List<String> SCOPES = Arrays.asList(
            CalendarScopes.CALENDAR_READONLY,
            "https://www.googleapis.com/auth/plus.me",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/calendar"
    );

    private static final String clientId = "626288801350-vk790l2a56u0m25p63q36asu4tv7gnsr.apps.googleusercontent.com";
    private static final String clientSecret = "wHAYULXTwsZWMQ827ITPIEVr";

    public CalendarApi(Context context){
        calendarUpdatesChannel = new EventChannel(context){
            @Override
            public void onEvent(EventType type ,String payload){
                if(type == EventType.CalendarUpdated){
                    calendarUpdates.onNext(payload);
                }
            }
        };
        calendarUpdatesChannel.listen();
    }


    /**
     * Access tokens have limited lifetimes. 
     * If the application needs access to a Google API beyond the lifetime of a single access token,
     * it can use a refresh token. A refresh token allows the application to obtain new access tokens. 
     * Use the refresh token to get a new access token on expiration
     * @param refreshToken the refresh token used for the calendar API
     */
    public Single<TokenResponse> getAccessTokenFromRefreshToken(String refreshToken){
        return Single.fromCallable(
            () ->
                new RefreshTokenRequest(
                        HTTP_TRANSPORT,
                        JacksonFactory.getDefaultInstance(),
                        new GenericUrl("https://www.googleapis.com/oauth2/v4/token"),
                        refreshToken
                )
                        .setClientAuthentication(
                                new BasicAuthentication(clientId, clientSecret)
                        ).execute()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Use the serverAuthCode to obtain a refresh token.
     * A refresh token allows the application to obtain new access tokens. 
     * @param serverAuthCode the code used for authentification
     */
    public Single<GoogleTokenResponse> getRefreshToken(final String serverAuthCode) {
        return Single.fromCallable(
                () ->
                    new GoogleAuthorizationCodeTokenRequest(
                            HTTP_TRANSPORT,
                            JacksonFactory.getDefaultInstance(),
                            "https://www.googleapis.com/oauth2/v4/token",
                            clientId,
                            clientSecret,
                            serverAuthCode,
                            ""
                    )
                            .setScopes(SCOPES)
                            .execute()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Obtains OAuth 2.0 credentials such as a client ID and client secret that
     * are known to both Google and the application. 
     * @param oauthToken token used to obtain credentials
     */
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
            );
    }

    /**
     * Get the id of the office hours calendar and if it doesn't exist create one
     * @param credential OAuth 2.0 credentials
     */
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

    /**
     * Get all events of the office hour calendar in the specified time range
     * @param calendarId the id of the office hour calendar
     * @param credential OAuth 2.0 credentials
     * @param start the starting point where to retrieve events
     * @param end the ending point where to retrieve events
     */
    public Observable<List<Event>> getEvents(final String calendarId, final Credential credentials, final DateTime start, final DateTime end) {
        return calendarUpdates
                .startWith(calendarId)
                .filter(modifiedCalendar -> modifiedCalendar.equals(calendarId))
                .observeOn(Schedulers.io())
                .map(
                        modifiedCalendar -> {
                            final Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), credentials)
                                    .setApplicationName("Pfoertner")
                                    .build();

                            Log.d(TAG, "About to download events for calendar " + calendarId);

                            final Events events = service.events().list(calendarId)
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
