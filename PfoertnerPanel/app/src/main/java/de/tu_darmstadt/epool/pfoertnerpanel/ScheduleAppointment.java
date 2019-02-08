package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.threeten.bp.LocalDateTime;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.RequestTask;

public class ScheduleAppointment extends AppCompatActivity {
    LocalDateTime now;
    String TAG = "Schedule ";
    DayView day = null;
    DayView days[];


    private static String OAUTH2 = "4/6QAxEqPuggLbxOli8_qoybBrYx9mltS-snaBAUR4zozGFEnm1wNbrHcmX0SDX6prrT1xhAjWLtqMu6KeciicAD8";
    private static String ACCESS_TOKEN = "ya29.GluoBmlsITwsJGYAQQpNLSCypo6wYgSaNChLfJ7jZvxjPws87V9wbZpdXaD8OSbE14dsjjQCO2qy7L-kymlXULOqHDMkx7hU-7KmSkTsfxo5UHAl-rwfm40Kk0ZN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
