package de.tu_darmstadt.epool.pfoertnerpanel;

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
    private final int selected = 0xFFFF4081;
    private Timehelpers timehelper;


    private Member appointmentMember;
    private List<Event> upcommingEvents;
    private LinkedList<DayView> upcommingDayViews;

    private LinearLayout timeslots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule_appointment);
        LinearLayout dayviews = (LinearLayout) findViewById(R.id.dayviews);
        timehelper = new Timehelpers();
        timeslots = (LinearLayout) findViewById(R.id.timeslots);
        now = LocalDateTime.now();


//        app = PfoertnerApplication.get(this);
//
//        appointmentMember = app
//                .getOffice()
//                .getMembers()
//                .stream()
//                .filter(member -> member.getCalendarApi() != null && member.getCalendarId() != null)
//                .findAny()
//                .get();
//
//        this.calendarApi = appointmentMember.getCalendarApi();



//        getEventsForTimeslots();
        //TODO: vorsicht async
        initializeDayViews(dayviews);
    }

    private void recolorDayViews(DayView selectedDayView){
        for (DayView d : upcommingDayViews) {
            d.setColor();
        }
        selectedDayView.setBackgroundColor(selected);
    }

    private void createTimeSlotsPerDay(DayView dayview){
        timeslots.removeAllViews();
        for (Event e : dayview.getEvents()){
            TimeslotView timeslot = new TimeslotView(this, e);
            timeslots.addView(timeslot);
        }
        recolorDayViews(dayview);
    }

    private void initializeDayViews(LinearLayout dayviews){
        upcommingDayViews = new LinkedList<DayView>();
        for(int i = 0; i < 28; i++){

            DayView dayview = new DayView(this);
            dayview.setDate(now.plusDays(i).withHour(0).withMinute(0));
//            addEventsToDayView(dayview);
            dayview.setColor();
            dayview.setOnClickListener((View v) -> createTimeSlotsPerDay(dayview));


            final FrameLayout.LayoutParams timeSlotMarginParams = (FrameLayout.LayoutParams) dayviews.getLayoutParams();
            timeSlotMarginParams.setMargins(0, 1, 0, 1);
            dayviews.addView(dayview, timeSlotMarginParams);
            upcommingDayViews.add(dayview);
        }
    }

    private void addEventsToDayView(DayView dayview){
        for (Event e : upcommingEvents){
            if(dayview.getDate() == timehelper.toLocalDateTime(e.getStart())){ //TODO: das geht so nicht richtig now <= ex <= ti aber ti at 00:00 ... differenzen testen ob das bei set date auch wirklich so geht
                dayview.addEvents(e);
            }
        }
    }

    private void getEventsForTimeslots(){
        new RequestTask<List<Event>>() {
            @Override
            protected List<Event> doRequests() throws Exception {
                return calendarApi.getEvents(
                        new DateTime(System.currentTimeMillis()),
                        new DateTime(System.currentTimeMillis() + 86400000 *28)
                ); //zeitraum 4 wochen
            }

            @Override
            protected void onSuccess(final List<Event> upcommingEvents) {
                NewScheduleAppointment.this.upcommingEvents = upcommingEvents;
            }

            @Override
            protected void onException(Exception e) {
                Log.e(TAG, "Failed to retrieve events.", e);
            }
        }.execute();
    }
}
