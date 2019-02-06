package de.tu_darmstadt.epool.pfoertnerpanel;

import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;

public class ScheduleAppointment extends AppCompatActivity {
    LocalDateTime now;
    String TAG = "Schedule ";
    DayView day = null;
    DayView days[];


    private static String CLIENT_ID = "245397489411-pm7par3q4kjlik44v5ofqfsrdtnb5k3a.apps.googleusercontent.com";
    private static String CLIENT_SECRET = "l2uMcyKEIGug9ZpiGWOMmj0p";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static String OAUTH2 = "4/6QBy_77oPImozNez-pxefmwAIXYSBr56a9l_KFfncYOqn_TKpHCGcVbiy-BoQnAFEHcRrzjOJED4j_1RpZnDCJA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new RequestTask<Void>(){
            @Override
            public Void doRequests(){
                try {
                    final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

                    Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(OAUTH2);
                    credential = new GoogleCredential.Builder()
                            .setTransport(HTTP_TRANSPORT)
                            .setJsonFactory(JacksonFactory.getDefaultInstance())
                            .setClientSecrets(CLIENT_ID,CLIENT_SECRET)
                            .build();
                    credential.setAccessToken(OAUTH2);
                    Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JacksonFactory.getDefaultInstance(), credential)
                            .setApplicationName("Pfoertner")
                            .build();

                    DateTime now = new DateTime(System.currentTimeMillis());
                    Events events = service.events().list("primary")
                            .setMaxResults(10)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> items = events.getItems();
                    for (Event event : items) {
                        Log.d("Calendar", event.getStart().toPrettyString());
                    }
                } catch (RuntimeException e){
                    e.printStackTrace();
                }

                catch (IOException e){
                    Log.d("Calendar","IOException");
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onException(Exception e){
                e.printStackTrace();
            }

            @Override
            protected void onSuccess(final Void result){

            }
        }.execute();

        setContentView(R.layout.activity_schedule_appointment);
        days = new DayView[10];


        days[0] = (DayView) findViewById(R.id.day0);
        days[0].setTitle("Mo");
        days[1] = (DayView) findViewById(R.id.day1);
        days[1].setTitle("Tue");
        days[2] = (DayView) findViewById(R.id.day2);
        days[2].setTitle("Wed");
        days[3] = (DayView) findViewById(R.id.day3);
        days[3].setTitle("Thu");
        days[4] = (DayView) findViewById(R.id.day4);
        days[4].setTitle("Fri");
        days[5] = (DayView) findViewById(R.id.day5);
        days[5].setTitle("Mo");
        days[6] = (DayView) findViewById(R.id.day6);
        days[6].setTitle("Tue");
        days[7] = (DayView) findViewById(R.id.day7);
        days[7].setTitle("Wed");
        days[8] = (DayView) findViewById(R.id.day8);
        days[8].setTitle("Thu");
        days[9] = (DayView) findViewById(R.id.day9);
        days[9].setTitle("Fri");

        //ueberlegungen
        now = LocalDateTime.now();

        switch (now.getDayOfWeek().toString()){
            case "Monday":
                colorDays(1);
                setDate(1);
                break;
            case "Tuesday":
                colorDays(2);
                setDate(2);
                break;
            case "Wednesday":
                colorDays(3);
                setDate(3);
                break;
            case "Thursday":
                setDate(4);
                colorDays(4);
                break;
            case "Friday":
                setDate(5);
                colorDays(5);
                break;
            default:
                colorDays(0);
                break;
        }


    }

    public void day0(View view){
        Log.d(TAG, "day0");
    }

    public void day1(View view){
        Log.d(TAG, "day1");
    }

    public void day2(View view){
        Log.d(TAG, "day2");
    }

    public void day3(View view){
        Log.d(TAG, "day3");
    }

    public void day4(View view){
        Log.d(TAG, "day4");
    }

    public void day5(View view){
        Log.d(TAG, "day5");
    }

    public void day6(View view){
        Log.d(TAG, "day6");
    }

    public void day7(View view){
        Log.d(TAG, "day7");
    }

    public void day8(View view){
        Log.d(TAG, "day8");
    }

    public void day9(View view){
        Log.d(TAG, "day9");
    }

    private void setDate(int start){

    }

    private void colorDays(int start){
        for(int i = 0;i<start;i++){
            days[i].setBackgroundColor(0xbdbdbdff);
        }
        for(int i = start;i<10;i++){
            days[i].setDate(now.getDayOfMonth()+"-"+now.getMonthValue());
            now.plusDays(100);
        }
    }


}
