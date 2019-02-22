package de.tu_darmstadt.epool.pfoertneradmin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.LocalCalendar;

public class AppointmentRequest extends AppCompatActivity{

    private static String TAG = "AppointmentRequest";
    private static int WRITE_CALENDAR_PERMISSION_REQUEST = 0;
    private SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Date start = null;
    private Date end = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        try {
            JSONObject appointmentJSON = new JSONObject(extras.getString("data"));
            Date start = dateParser.parse(appointmentJSON.getString("start"));
            Date end = dateParser.parse(appointmentJSON.getString("end"));

            Log.d(TAG,start.toString());
            Log.d(TAG,end.toString());

            writeCalendarEvent(start,end);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG,"Could not parse appointment request json");
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d(TAG,"Could not parse appointment date");
        }

    }

    private void writeCalendarWithPermission(Date start, Date end){
        try{
            LocalCalendar.getInstance(this).writeEvent(start,end,"","","");
            finish();
        } catch (SecurityException e){
            // This point in the code should never be reached
            ErrorInfoDialog.show(this, "You have to grant the permission to write into the calendar", aVoid ->writeCalendarEvent(start,end));
        }
    }

    private void writeCalendarEvent(Date start, Date end){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    WRITE_CALENDAR_PERMISSION_REQUEST);
            this.start = start;
            this.end = end;
        } else {
            writeCalendarWithPermission(start,end);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        if(requestCode == WRITE_CALENDAR_PERMISSION_REQUEST &&
                grantResults.length>0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(this.start != null && this.end != null){
                writeCalendarWithPermission(start,end);
                this.start = null;
                this.end = null;
            }
        } else if(this.start != null && this.end != null) {
            // The permission was not granted, but the user requested writing into the calendar
            ErrorInfoDialog.show(this, "You have to grant the permission to write into the calendar", aVoid ->writeCalendarEvent(start,end));
        }
    }
}
