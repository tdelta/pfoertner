package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.CalendarApi;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.helpers.Timehelpers;

public class NewScheduleAppointment extends AppCompatActivity {
    private final String TAG = "NewScheduleAppointment";
    private PfoertnerApplication app;
    private CalendarApi calendarApi;
    private LocalDateTime now;
    private Timehelpers timehelper;
    private Member appointmentMember;
    private List<Event> upcommingEvents;
    private LinkedList<DayView> upcommingDayViews;
    private LinearLayout dayviews;
    private LinearLayout timeslots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule_appointment);
        dayviews = (LinearLayout) findViewById(R.id.dayviews);
        timehelper = new Timehelpers();
        timeslots = (LinearLayout) findViewById(R.id.timeslots);
        now = LocalDateTime.now();
        app = PfoertnerApplication.get(this);

        Optional<Member> tempMember = app
                .getOffice()
                .getMemberById(getIntent().getIntExtra("MemberId",-1));
        Log.d(TAG, getIntent().getIntExtra("MemberId",-1) + "");
        if(tempMember.isPresent()){
            appointmentMember = tempMember.get();
            calendarApi = appointmentMember.getCalendarApi();
            getEventsForTimeslots();
        }else{
            //TODO: go back to main activity
            Log.d(TAG, "no member present");
        }
    }

    private void recolorDayViews(DayView selectedDayView){
        for (DayView d : upcommingDayViews) {
            d.setColor();
        }
        selectedDayView.setBackgroundColor(0xFFFF4081);
    }

    private void createTimeSlotsPerDay(DayView dayview){
        timeslots.removeAllViews();
        for (Event e : dayview.getEvents()){
            TimeslotView timeslot = new TimeslotView(this, e);
            timeslot.setOnClickListener((View v) -> gotoMakeAppointment(v, timeslot));
            final FrameLayout.LayoutParams timeslotMarginParams = (FrameLayout.LayoutParams) timeslots.getLayoutParams();
            timeslotMarginParams.setMargins(1, 1, 1, 1);
            timeslots.addView(timeslot, timeslotMarginParams);
        }
        recolorDayViews(dayview);
    }

    private void initializeDayViews(){
        upcommingDayViews = new LinkedList<DayView>();
        for(int i = 0; i < 28; i++){
            DayView dayview = new DayView(this);
            dayview.setDate(now.plusDays(i).withHour(0).withMinute(0));
            addEventsToDayView(dayview);
            dayview.setColor();
            dayview.setOnClickListener((View v) -> createTimeSlotsPerDay(dayview));
            final FrameLayout.LayoutParams dayViewMarginParams = (FrameLayout.LayoutParams) dayviews.getLayoutParams();
            dayViewMarginParams.setMargins(0, 1, 0, 1);
            dayviews.addView(dayview, dayViewMarginParams);
            upcommingDayViews.add(dayview);
        }
    }

    private void addEventsToDayView(DayView dayview){
        for (Event e : upcommingEvents){
            if(timehelper.isItToday(dayview.getDate(),timehelper.toLocalDateTime(e.getStart()))){
                dayview.addEvents(e);
//                upcommingEvents.remove(e);
            }
        }
    }

    private void getEventsForTimeslots(){
        new RequestTask<List<Event>>() {
            @Override
            protected List<Event> doRequests() throws Exception {
                DateTime start = new DateTime(System.currentTimeMillis());
                DateTime end = new DateTime(System.currentTimeMillis() + 86400000L *28);
                return calendarApi.getEvents(
                        start,end //zeitraum 4 wochen
                );
            }

            @Override
            protected void onSuccess(final List<Event> upcommingEvents) {
                NewScheduleAppointment.this.upcommingEvents = upcommingEvents;
                initializeDayViews();
            }

            @Override
            protected void onException(Exception e) {
                Log.e(TAG, "Failed to retrieve events.", e);
            }
        }.execute();
    }

    public void gotoMakeAppointment(View view, TimeslotView timeslot) {
        final Intent intent = new Intent(this, MakeAppointment.class);

        Event e = timeslot.getEvent();



        intent.putExtra("appointmentStartTimeHour", timehelper.toLocalDateTime(e.getStart()).getHour());
        if(timehelper.toLocalDateTime(e.getStart()).getMinute() > 9){
            intent.putExtra("appointmentStartTimeMinutes", "" + timehelper.toLocalDateTime(e.getStart()).getMinute());
        }else{
            intent.putExtra("appointmentStartTimeMinutes", "0" + timehelper.toLocalDateTime(e.getStart()).getMinute());
        }
        intent.putExtra("appointmentEndTimeHour", timehelper.toLocalDateTime(e.getEnd()).getHour());
        if(timehelper.toLocalDateTime(e.getEnd()).getMinute() > 9){
            intent.putExtra("appointmentEndTimeMinutes", "" + timehelper.toLocalDateTime(e.getEnd()).getMinute());
        }else{
            intent.putExtra("appointmentEndTimeMinutes", "0" + timehelper.toLocalDateTime(e.getEnd()).getMinute());
        }
        intent.putExtra("Day", timehelper.toLocalDateTime(e.getStart()).getDayOfMonth());
        intent.putExtra("Month", timehelper.toLocalDateTime(e.getStart()).getMonthValue());
        intent.putExtra("Year", timehelper.toLocalDateTime(e.getStart()).getYear());
        intent.putExtra("MemberId", getIntent().getIntExtra("MemberId",-1));//TODO: 0 hardcoded

        // yyyy-MM-dd HH:mm
        startActivity(intent);
    }
}