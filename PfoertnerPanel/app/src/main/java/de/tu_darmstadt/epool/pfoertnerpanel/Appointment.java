package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;


import java.util.Calendar;

public class Appointment extends AppCompatActivity implements CalendarView.OnDateChangeListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment);


        CalendarView calendar = findViewById(R.id.calendarView);

        // set minimum date  to current day
        Calendar date = Calendar.getInstance();
        calendar.setMinDate(date.getTimeInMillis());

        // set maximum date to two years in the future
        date.add(Calendar.WEEK_OF_MONTH, 2);
        calendar.setMaxDate(date.getTimeInMillis());

        // use callback from this class
        calendar.setOnDateChangeListener(this);
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, view.getDate());
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(builder.build());
        startActivity(intent);
    }
}
