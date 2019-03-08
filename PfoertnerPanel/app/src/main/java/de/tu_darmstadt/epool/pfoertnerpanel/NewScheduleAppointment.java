package de.tu_darmstadt.epool.pfoertnerpanel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.threeten.bp.LocalDateTime;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import de.tu_darmstadt.epool.pfoertner.common.CalendarApi;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.helpers.Timehelpers;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;

public class NewScheduleAppointment extends AppCompatActivity {
    private final String TAG = "NewScheduleAppointment";
    private LocalDateTime now;
    private Timehelpers timehelper;
    private List<Event> upcomingEvents;
    private LinkedList<DayView> upcomingDayViews;
    private LinearLayout dayviews;
    private LinearLayout timeslots;

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule_appointment);
        dayviews = (LinearLayout) findViewById(R.id.dayviews);
        timehelper = new Timehelpers();
        timeslots = (LinearLayout) findViewById(R.id.timeslots);
        now = LocalDateTime.now();

        int memberId = getIntent().getIntExtra("MemberId",-1);

        if (memberId < 0) {
            Log.e(TAG, "A member must be selected to show appointments.");
        }

        else {
            final TextView room = (TextView) findViewById(R.id.schedule_room);

            room.setText("A101"); //TODO: app.getoffice hat leider keinen raum wert...

            final PanelApplication app = PanelApplication.get(this);

            app
                    .getRepo()
                    .getMemberRepo()
                    .getMember(memberId)
                    .observe(this, member -> {
                        final TextView appointmentMemberName = (TextView) findViewById(R.id.appointment_member);

                        appointmentMemberName.setText("Appointment times for: " + member.getFirstName() + " " + member.getLastName());
                    });

            LiveData<MemberCalendarInfo> calendarInfoLiveData = app
                    .getPanelRepo()
                    .getMemberCalendarInfoRepo()
                    .getCalendarInfoByMemberId(memberId);

            LiveData<List<Appointment>> appointmentLiveData = app
                    .getRepo()
                    .getAppointmentRepository()
                    .getAppointmentsOfMember(memberId);

            calendarInfoLiveData.observe(
                    this,
                    calendarInfo -> updateEvents(memberId, appointmentLiveData.getValue(), calendarInfo)
            );

            appointmentLiveData.observe(
                    this,
                    appointments ->
                    {
                        if (calendarInfoLiveData.getValue() != null) {
                            updateEvents(memberId, appointments, calendarInfoLiveData.getValue());
                        }
                    }
            );
        }
    }

    private void updateEvents(int memberId, List<Appointment> appointments, MemberCalendarInfo calendarInfo){
        if (calendarInfo == null) {
            Log.e(TAG, "There is no member calendar info set for member " + memberId + ", we can not load time slots.");
        }

        else if (calendarInfo.getCalendarId() == null) {
            Log.e(TAG, "The member " + memberId + " does not have a calendar registered, we can not load time slots.");
        }

        else if (calendarInfo.getOAuthToken() == null) {
            Log.e(TAG, "We do not have an oauth token to access the calendar of member " + memberId + ".");
        }

        else {
            Log.d(TAG, "Downloading time slot events...");

            final DateTime start = new DateTime(System.currentTimeMillis());
            final DateTime end = new DateTime(System.currentTimeMillis() + 86400000L *28);

            disposables.add(getEvents(calendarInfo, start, end)
                    .subscribe(
                            events -> {
                                Log.d(TAG, "Got timeslot events: " + events.toString());

                                upcomingEvents = removeAcceptedAppointments(events,appointments);

                                initializeDayViews();
                            },
                            throwable -> Log.e(TAG, "Failed to fetch events.", throwable)
                    )
            );
        }
    }

    private List<Event> removeAcceptedAppointments(List<Event> timeslots,@Nullable List<Appointment> appointments){
        List<Event> result = new LinkedList<>();
        result.addAll(timeslots);

        if(appointments == null) return result;

        Log.d(TAG, "Number of appointments: "+appointments.size());

        for(Appointment appointment: appointments){
            Log.d(TAG,"Appointment accepted "+appointment.getAccepted());
            if(appointment.getAccepted()){
                Event timeslot;
                for(int i = 0;i < result.size(); i++){
                    timeslot = timeslots.get(i);

                    Log.d(TAG,"Appointment start: "+appointment.getStart().toString()+" Timeslot start: "+timeslot.getStart().getDateTime().toString());

                    long timeslotStartShift = timeslot.getStart().getDateTime().getTimeZoneShift() * 3600 * 1000L;
                    Date timeslotStart = new Date(timeslot.getStart().getDateTime().getValue()+timeslotStartShift);
                    long timeslotEndShift = timeslot.getEnd().getDateTime().getTimeZoneShift() * 3600 * 1000L;
                    Date timeslotEnd = new Date(timeslot.getEnd().getDateTime().getValue()+timeslotEndShift);

                    if(appointment.getEnd().after(timeslotStart) && appointment.getStart().before(timeslotEnd)){
                        // The appointment intersects the timeslot
                        result.remove(i);
                        i--;

                        if(appointment.getStart().after(timeslotStart)){
                            // Add a timeslot before the accepted appointment
                            Event newTimeslot = timeslot.clone();
                            newTimeslot.setStart(newTimeslot.getStart().setDateTime(new DateTime(appointment.getStart().getTime())));
                            result.add(newTimeslot);
                        }
                        if(appointment.getEnd().before(timeslotEnd)){
                            // Add a timeslot after the accepted appointment
                            Event newTimeslot = timeslot.clone();
                            newTimeslot.setStart(newTimeslot.getStart().setDateTime(new DateTime(appointment.getEnd().getTime())));
                            result.add(newTimeslot);
                        }
                    }
                }
            }
        }
        return result;
    }

    private Single<List<Event>> getEvents(final MemberCalendarInfo calendarInfo, final DateTime start, final DateTime end) {
        final PanelApplication app = PanelApplication.get(this);

        return app
                .getCalendarApi()
                .getCredential(calendarInfo.getOAuthToken())
                .flatMap(
                        credentials -> app
                                .getCalendarApi()
                                .getEvents(calendarInfo.getCalendarId(), credentials, start, end)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void recolorDayViews(DayView selectedDayView){
        for (DayView d : upcomingDayViews) {
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
        upcomingDayViews = new LinkedList<DayView>();
        for(int i = 0; i < 28; i++){
            DayView dayview = new DayView(this);
            dayview.setDate(now.plusDays(i).withHour(0).withMinute(0));
            addEventsToDayView(dayview);
            dayview.setColor();
            dayview.setOnClickListener((View v) -> createTimeSlotsPerDay(dayview));
            final FrameLayout.LayoutParams dayViewMarginParams = (FrameLayout.LayoutParams) dayviews.getLayoutParams();
            dayViewMarginParams.setMargins(0, 1, 0, 1);
            dayviews.addView(dayview, dayViewMarginParams);
            upcomingDayViews.add(dayview);
        }
    }

    private void addEventsToDayView(DayView dayview){
        for (Event e : upcomingEvents){
            if(timehelper.isItToday(dayview.getDate(),timehelper.toLocalDateTime(e.getStart()))){
                dayview.addEvents(e);
            }
        }
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
        intent.putExtra("MemberId", getIntent().getIntExtra("MemberId",-1));

        // yyyy-MM-dd HH:mm
        startActivity(intent);


        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        disposables.dispose();
    }
}