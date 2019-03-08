package de.tu_darmstadt.epool.pfoertnerpanel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import org.reactivestreams.Publisher;
import org.threeten.bp.LocalDateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.helpers.Timehelpers;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;
import android.arch.lifecycle.LiveDataReactiveStreams;

public class NewScheduleAppointment extends AppCompatActivity {
    private final String TAG = "NewScheduleAppointment";
    private LocalDateTime now;
    private Timehelpers timehelper;
    private HashMap<LocalDateTime, DayView> upcomingDayViews = new HashMap<>();
    private LinearLayout dayviews;
    private LinearLayout timeslots;
    private LocalDateTime selectedDay;

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule_appointment);
        dayviews = (LinearLayout) findViewById(R.id.dayviews);
        timehelper = new Timehelpers();
        timeslots = (LinearLayout) findViewById(R.id.timeslots);
        now = LocalDateTime.now();


        final TextView room = findViewById(R.id.schedule_room);

        PanelApplication app = PanelApplication.get(this);
        app.getRepo()
                .getOfficeRepo()
                .getOffice(app.getOffice().getId())
                .observe(this,
                        office -> {
                            if(office.getRoom()==null){
                                room.setText("Room Name Not Set");
                            } else {
                                room.setText(office.getRoom());
                            }
                        });

        int memberId = getIntent().getIntExtra("MemberId",-1);

        if (memberId < 0) {
            Log.e(TAG, "A member must be selected to show appointments.");
        }

        else {
            app
                    .getRepo()
                    .getMemberRepo()
                    .getMember(memberId)
                    .observe(this, member -> {
                        final TextView appointmentMemberName = (TextView) findViewById(R.id.appointment_member);
                        appointmentMemberName.setText("Appointment times for: " + member.getFirstName() + " " + member.getLastName());
                    });

            // ---------------
            // Build an rxjava pipeline from the database to the screen
            // ---------------

            // This block only converts livedata to observables
            LiveData<MemberCalendarInfo> calendarInfoLiveData = app
                    .getPanelRepo()
                    .getMemberCalendarInfoRepo()
                    .getCalendarInfoByMemberId(memberId);

            Publisher<MemberCalendarInfo> memberCalendarInfoPublisher = LiveDataReactiveStreams.toPublisher(this,calendarInfoLiveData);
            Observable<MemberCalendarInfo> calendarInfoObservable = Observable.fromPublisher(memberCalendarInfoPublisher);

            LiveData<List<Appointment>> appointmentLiveData = app
                    .getRepo()
                    .getAppointmentRepository()
                    .getAppointmentsOfMember(memberId);

            Publisher<List<Appointment>> appointmentPublisher = LiveDataReactiveStreams.toPublisher(this,appointmentLiveData);
            Observable<List<Appointment>> appointmentObservable = Observable.fromPublisher(appointmentPublisher);

            final DateTime start = new DateTime(System.currentTimeMillis());
            final DateTime end = new DateTime(System.currentTimeMillis() + 86400000L *28);

            Observable<List<Event>> calendarEventsObservable = calendarInfoObservable
                    .flatMapSingle(memberCalendarInfo -> getEvents(memberCalendarInfo,start,end));

            Observable.combineLatest(appointmentObservable,calendarEventsObservable,
                    (appointments,events) -> new Pair<List<Event>,List<Appointment>>(events,appointments))
                    .map(this::removeAcceptedAppointments)
                    .subscribe(
                            events -> initializeDayViews(events),
                            throwable -> Log.e(TAG,"Could not load events and appointments",throwable)
                    );

        }
    }

    private List<Event> removeAcceptedAppointments(Pair<List<Event>,List<Appointment>> eventsAndAppointments){
        List<Event> result = new LinkedList<>(eventsAndAppointments.first);

        List<Appointment> appointments = eventsAndAppointments.second;
        if(appointments == null) {
            return result;
        }

        Log.d(TAG, "Number of appointments: "+appointments.size());

        for(Appointment appointment: appointments){
            Log.d(TAG,"Appointment accepted "+appointment.getAccepted());
            if(appointment.getAccepted()){
                for(Event timeslot: eventsAndAppointments.first){

                    long timeslotStartShift = timeslot.getStart().getDateTime().getTimeZoneShift() * 3600 * 1000L;
                    Date timeslotStart = new Date(timeslot.getStart().getDateTime().getValue()+timeslotStartShift);
                    long timeslotEndShift = timeslot.getEnd().getDateTime().getTimeZoneShift() * 3600 * 1000L;
                    Date timeslotEnd = new Date(timeslot.getEnd().getDateTime().getValue()+timeslotEndShift);

                    if(appointment.getEnd().after(timeslotStart) && appointment.getStart().before(timeslotEnd)){
                        // The appointment intersects the timeslot
                        result.remove(timeslot);
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
                        credentials -> {
                            if (calendarInfo == null) {
                                throw new RuntimeException( "There is no member calendar info set we can not load time slots.");
                            }

                            else if (calendarInfo.getCalendarId() == null) {
                                throw new RuntimeException( "The member " + calendarInfo.getMemberId() + " does not have a calendar registered, we can not load time slots.");
                            }

                            else if (calendarInfo.getOAuthToken() == null) {
                                throw new RuntimeException( "We do not have an oauth token to access the calendar of member " + calendarInfo.getMemberId() + ".");
                            } else {
                                return app
                                        .getCalendarApi()
                                        .getEvents(calendarInfo.getCalendarId(), credentials, start, end);
                            }
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void recolorDayViews(DayView selectedDayView){
        for (DayView d : upcomingDayViews.values()) {
            d.setColor();
        }
        selectedDayView.setBackgroundColor(0xFFFF4081);
    }

    private void createTimeSlotsPerDay(DayView dayview){
        selectedDay = dayview.getDate();
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

    private void initializeDayViews(List<Event> upcomingEvents){
        dayviews.removeAllViews();
        upcomingDayViews.clear();
        for(int i = 0; i < 28; i++){
            DayView dayview = new DayView(this);
            LocalDateTime date = now.plusDays(i).withHour(0).withMinute(0);
            dayview.setDate(date);
            addEventsToDayView(dayview,upcomingEvents);
            dayview.setColor();
            dayview.setOnClickListener((View v) -> createTimeSlotsPerDay(dayview));
            final FrameLayout.LayoutParams dayViewMarginParams = (FrameLayout.LayoutParams) dayviews.getLayoutParams();
            dayViewMarginParams.setMargins(0, 1, 0, 1);
            dayviews.addView(dayview, dayViewMarginParams);
            upcomingDayViews.put(date,dayview);
        }

        if(selectedDay != null) {
            createTimeSlotsPerDay(upcomingDayViews.get(selectedDay));
        }
    }

    private void addEventsToDayView(DayView dayview, List<Event> upcomingEvents){
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