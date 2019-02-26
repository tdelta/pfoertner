package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.api.services.calendar.model.Event;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.CalendarApi;
import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.RequestTask;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class ScheduleAppointment extends AppCompatActivity {
    private LocalDateTime now;
    private String TAG = "Schedule ";
    private DayView days[];
    private final int selected = 0xFFFF4081;
    private final int nothing = 0xFF808080;
    private final int normal = 0xFF8BC34A;
    private int currentDay;
    private LinearLayout slots;
    private List<LinkedList<String>> calendarSlots;
    private TextView officeHours;
    private CalendarApi calendarApi;
    private DateTime todayTime;
    private DateTime endTime;
    private PfoertnerApplication app;
    private Member appointmentMember;
    private LocalDateTime selectedDay;

    private static class CalendarSlot {
        public final LocalDateTime startDay;
        public final LocalDateTime endDay;

        public CalendarSlot(final LocalDateTime startDay, final LocalDateTime endDay) {
            this.startDay = startDay;
            this.endDay = endDay;
        }

        public String makeLabel() {
            return startDay.getHour() + ":" + startDay.getMinute() + "-" + endDay.getHour() + ":" + endDay.getMinute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment);
        now = LocalDateTime.now();
        days = new DayView[12];
        slots = (LinearLayout) findViewById(R.id.officehours);
        calendarSlots = new ArrayList<LinkedList<String>>(Collections.nCopies(12, new LinkedList<>()));


        app = PfoertnerApplication.get(this);

        appointmentMember = app
                .getOffice()
                .getMembers()
                .stream()
                .filter(member -> member.getCalendarApi() != null && member.getCalendarId() != null)
                .findAny()
                .get();

        this.calendarApi = appointmentMember.getCalendarApi();

//        appointmentMember = app.getOffice().getMembers().get(0);
////        for (Member m : app.getOffice().getMembers()){
////
////            if(m.getMemberId() == 0){// TODO: 0 => platzhalter
////                 appointmentMember = m;
////            }
////        }
//
//
//        calendarApi = appointmentMember.getCalendarApi();
        todayTime = new DateTime(System.currentTimeMillis());
        // 86400000 = 1Tag, 14 = 2 Wochen
        endTime = new DateTime(System.currentTimeMillis() + 86400000 *14);

        Log.d(TAG,"Heute ist " + todayTime.toString());
        Log.d(TAG,"Dann ist " + endTime.toString());



        LinkedList<String> test = new LinkedList<>();
        test.add("11:40 - 12:40");
        test.add("13:40 - 14:40");
        test.add("14:40 - 15:40");
        test.add("15:40 - 16:40");
        test.add("16:40 - 17:40");
        test.add("17:40 - 18:40");
        test.add("11:40 - 12:40");
        test.add("13:40 - 14:40");
        test.add("14:40 - 15:40");
        test.add("15:40 - 16:40");
        test.add("16:40 - 17:40");
        test.add("17:40 - 18:40");
        test.add("11:40 - 12:40");
        test.add("13:40 - 14:40");
        test.add("14:40 - 15:40");
        test.add("15:40 - 16:40");
        test.add("16:40 - 17:40");
        test.add("17:40 - 18:40");
        test.add("11:40 - 12:40");
        test.add("13:40 - 14:40");
        test.add("14:40 - 15:40");
        test.add("15:40 - 16:40");
        test.add("16:40 - 17:40");
        test.add("17:40 - 18:40");

        //calendarSlots.set(7, test);
        //calendarSlots.set(9, test);
        //calendarSlots.set(11, test);



//        for(int i = 0;i<12;i++){
//            if (i!= 5 && i != 6) {
//                String abc = "day" + i;
//                int resID = getResources().getIdentifier(abc, "id", getPackageName());
//                days[i] = (DayView) findViewById(resID);
//            }
//        }

//
        days[0] = (DayView) findViewById(R.id.day0);
        days[1] = (DayView) findViewById(R.id.day1);
        days[2] = (DayView) findViewById(R.id.day2);
        days[3] = (DayView) findViewById(R.id.day3);
        days[4] = (DayView) findViewById(R.id.day4);
        days[7] = (DayView) findViewById(R.id.day5);
        days[8] = (DayView) findViewById(R.id.day6);
        days[9] = (DayView) findViewById(R.id.day7);
        days[10] = (DayView) findViewById(R.id.day8);
        days[11] = (DayView) findViewById(R.id.day9);

        switch (now.getDayOfWeek().toString()){
            case "MONDAY":
                currentDay = 0;
                colorDaysForDayViews(currentDay);
                setDateForDayViews(currentDay,0);
                break;
            case "TUESDAY":
                currentDay = 1;
                colorDaysForDayViews(currentDay);
                setDateForDayViews(currentDay,0);
                break;
            case "WEDNESDAY":
                currentDay = 2;
                colorDaysForDayViews(currentDay);
                setDateForDayViews(currentDay,0);
                break;
            case "THURSDAY":
                currentDay = 3;
                setDateForDayViews(currentDay,0);
                colorDaysForDayViews(currentDay);
                break;
            case "FRIDAY":
                currentDay = 4;
                setDateForDayViews(currentDay,0);
                colorDaysForDayViews(currentDay);
                break;
            default:
                colorDaysForDayViews(currentDay);
                switch (now.getDayOfWeek().toString()){
                    case "SATURDAY":
                        currentDay = 0;
                        setDateForDayViews(currentDay,2);
                        break;
                    case "SUNDAY":
                        currentDay = 0;
                        setDateForDayViews(currentDay,1);
                        break;
                }
                break;
        }
        setEventsForTimeslots(currentDay);
        officeHours = findViewById(R.id.textView4);
    }

    public void onDayPressed(final View view){
        colorDaysForDayViews(normal);
        slots.removeAllViews();

        switch (view.getId()){
            case R.id.day0:
                updateDayViewButton(0);
                break;
            case R.id.day1:
                updateDayViewButton(1);
                break;
            case R.id.day2:
                updateDayViewButton(2);
                break;
            case R.id.day3:
                updateDayViewButton(3);
                break;
            case R.id.day4:
                updateDayViewButton(4);
                break;
            case R.id.day5:
                updateDayViewButton(7);
                break;
            case R.id.day6:
                updateDayViewButton(8);
                break;
            case R.id.day7:
                updateDayViewButton(9);
                break;
            case R.id.day8:
                updateDayViewButton(10);
                break;
            case R.id.day9:
                updateDayViewButton(11);
                break;
        }
    }

    private void updateDayViewButton(final int dayOffset){
        days[dayOffset].setBackgroundColor(selected);

        createTimeSlot(dayOffset);

        selectedDay = days[dayOffset].getDate();
    }

    private void setDateForDayViews(final int start, final int weekend){
        for(int i = start; i < 12; ++i) {
            if (i != 5 && i != 6){
                days[i].setDate(now.plusDays(i-start+weekend));
            }
        }

        for(int i = 0; i < start; ++i){
            days[i].setDate(now.plusDays(i-start));
        }
    }

    private void colorDaysForDayViews(final int start){
        for(int i = 0; i < 12; ++i){
            if (i != 5 && i != 6) {
                if(calendarSlots.get(i) == null || i < start){
                    days[i].setBackgroundColor(nothing);
                }

                else {
                    days[i].setBackgroundColor(normal);
                }
            }
        }
    }

    private void createTimeSlot(final int day){
        if(calendarSlots.get(day) != null) {
            officeHours.setText("Available office hours");

            for (final String appointmentTime : calendarSlots.get(day)) {
                final TimeslotView timeSlot = new TimeslotView(this);

                timeSlot.setAppointmentTime(appointmentTime);
                timeSlot.setOnClickListener(v -> gotoMakeAppointment(v, appointmentTime, day));

                final FrameLayout.LayoutParams timeSlotMarginParams = (FrameLayout.LayoutParams) slots.getLayoutParams();

                timeSlotMarginParams.setMargins(0, 0, 15, 0);

                slots.addView(timeSlot, timeSlotMarginParams);
            }
        }

        else {
            officeHours.setText("Sorry no office hours on this day");
        }
    }

    private static LocalDateTime toLocalDateTime(final EventDateTime edt) {
        final DateTime calendarDateTime = edt.getDateTime();
        final int timeShiftInMinutes = calendarDateTime.getTimeZoneShift();

        return LocalDateTime.ofEpochSecond(
                calendarDateTime.getValue() / 1000,
                0,
                ZoneOffset.ofHoursMinutes(timeShiftInMinutes / 60, timeShiftInMinutes % 60)
        );
    }

    private void setEventsForTimeslots(final int day){
        new RequestTask<List<Event>>() {
            @Override
            protected List<Event> doRequests() throws Exception {
                return calendarApi.getEvents(todayTime, endTime);
            }

            @Override
            protected void onSuccess(final List<Event> upcommingEvents) {
                for (final Event e : upcommingEvents) {
                    final LocalDateTime startDay = toLocalDateTime(e.getStart());
                    final Duration timePassed = Duration.between(now, startDay);
                    final LocalDateTime endDay = toLocalDateTime(e.getEnd());

                    switch (startDay.getDayOfWeek().toString()){
                        case "MONDAY":
                            setTimeSlotCaption(timePassed.toDays(), day, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                            break;
                        case "TUESDAY":
                            setTimeSlotCaption(timePassed.toDays(), day + 1, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                            break;
                        case "WEDNESDAY":
                            setTimeSlotCaption(timePassed.toDays(), day + 2, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                            break;
                        case "THURSDAY":
                            setTimeSlotCaption(timePassed.toDays(), day + 3, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                            break;
                        case "FRIDAY":
                            setTimeSlotCaption(timePassed.toDays(), day + 4, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            protected void onException(Exception e) {
                Log.e(TAG, "Failed to retrieve events.", e);
            }
        }.execute();
    }

    private void setTimeSlotCaption(float passed, int day, int startHours, int startMinutes, int endHours, int endMinutes){
        if(passed < day){
            calendarSlots.get(day).add(startHours + ":" + startMinutes + "-" + endHours + ":" + endMinutes);
        }else{
            calendarSlots.get(day+7).add(startHours + ":" + startMinutes + "-" + endHours + ":" + endMinutes);
        }
    }

    public void gotoMakeAppointment(View view, String time, int day) {
        final Intent intent = new Intent(this, MakeAppointment.class);
        intent.putExtra("appointmentTime", time);
        intent.putExtra("Day", selectedDay.getDayOfMonth());
        intent.putExtra("Month", selectedDay.getMonthValue());
        intent.putExtra("Year", selectedDay.getYear());
        intent.putExtra("MemberId", 0);//TODO: 0 hardcoded

        // yyyy-MM-dd HH:mm
        startActivity(intent);
    }
}