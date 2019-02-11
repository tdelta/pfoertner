package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.api.services.calendar.model.Event;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.CalendarApi;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

public class ScheduleAppointment extends AppCompatActivity {
    LocalDateTime now;
    String TAG = "Schedule ";
    DayView days[];
    final int selected = 0xFFFF4081;
    final int nothing = 0xFF808080;
    final int normal = 0xFF8BC34A;
    int currentDay;
    LinearLayout slots;
    LinkedList<String>[] calendarSlots;
    TextView officeHours;
    CalendarApi calendarApi;
    DateTime todayTime;
    DateTime endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment);
        now = LocalDateTime.now();
        days = new DayView[12];
        slots = (LinearLayout) findViewById(R.id.officehours);
        calendarSlots = new LinkedList[12];

        Member testMember = null;

////        calendarApi = testMember.getCalendarApi();

        todayTime = new DateTime(System.currentTimeMillis());
        // 86400000 = 1Tag, 14 = 2 Wochen
        endTime = new DateTime(System.currentTimeMillis() + 86400000 *14);

        Log.d(TAG,"Heute ist " + todayTime.toString());
        Log.d(TAG,"Dann ist " + endTime.toString());



//        LinkedList<String> test =
////        test.add("11:40 - 12:40");
////        test.add("13:40 - 14:40");
////        test.add("14:40 - 15:40");
////        test.add("15:40 - 16:40");
////        test.add("16:40 - 17:40");
////        test.add("17:40 - 18:40");
////        test.add("11:40 - 12:40");
////        test.add("13:40 - 14:40");
////        test.add("14:40 - 15:40");
////        test.add("15:40 - 16:40");
////        test.add("16:40 - 17:40");
////        test.add("17:40 - 18:40");
////        test.add("11:40 - 12:40");
////        test.add("13:40 - 14:40");
////        test.add("14:40 - 15:40");
////        test.add("15:40 - 16:40");
////        test.add("16:40 - 17:40");
////        test.add("17:40 - 18:40");
////        test.add("11:40 - 12:40");
////        test.add("13:40 - 14:40");
////        test.add("14:40 - 15:40");
////        test.add("15:40 - 16:40");
////        test.add("16:40 - 17:40");
////        test.add("17:40 - 18:40");

        for(LinkedList<String> l: calendarSlots){
            l = new LinkedList<String>();
        }


//        calendarSlots[7] = test;
//        calendarSlots[9] = test;
//        calendarSlots[11] = test;

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
        days[7] = (DayView) findViewById(R.id.day5);
        days[7].setTitle("Mo");
        days[8] = (DayView) findViewById(R.id.day6);
        days[8].setTitle("Tue");
        days[9] = (DayView) findViewById(R.id.day7);
        days[9].setTitle("Wed");
        days[10] = (DayView) findViewById(R.id.day8);
        days[10].setTitle("Thu");
        days[11] = (DayView) findViewById(R.id.day9);
        days[11].setTitle("Fri");


        switch (now.getDayOfWeek().toString()){
            case "MONDAY":
                currentDay = 0;
                colorDays(currentDay);
                setDate(currentDay,0);
                break;
            case "TUESDAY":
                currentDay = 1;
                colorDays(currentDay);
                setDate(currentDay,0);
                break;
            case "WEDNESDAY":
                currentDay = 2;
                colorDays(currentDay);
                setDate(currentDay,0);
                break;
            case "THURSDAY":
                currentDay = 3;
                setDate(currentDay,0);
                colorDays(currentDay);
                break;
            case "FRIDAY":
                currentDay = 4;
                setDate(currentDay,0);
                colorDays(currentDay);
                break;
            default:
                colorDays(currentDay);
                switch (now.getDayOfWeek().toString()){
                    case "SATURDAY":
                        currentDay = 0;
                        setDate(currentDay,2);
                        break;
                    case "SUNDAY":
                        currentDay = 0;
                        setDate(currentDay,1);
                        break;
                }
                break;
        }
        setEvents(currentDay);
        officeHours = findViewById(R.id.textView4);
    }

    public void weekDay(View view){
        colorDays(normal);
        slots.removeAllViews();

        switch (view.getId()){
            case R.id.day0:
                days[0].setBackgroundColor(selected);
                createTimeSlot(0);
                break;
            case R.id.day1:
                days[1].setBackgroundColor(selected);
                createTimeSlot(1);
                break;
            case R.id.day2:
                days[2].setBackgroundColor(selected);
                createTimeSlot(2);
                break;
            case R.id.day3:
                days[3].setBackgroundColor(selected);
                createTimeSlot(3);
                break;
            case R.id.day4:
                days[4].setBackgroundColor(selected);
                createTimeSlot(4);
                break;
            case R.id.day5:
                days[7].setBackgroundColor(selected);
                createTimeSlot(7);
                break;
            case R.id.day6:
                days[8].setBackgroundColor(selected);
                createTimeSlot(8);
                break;
            case R.id.day7:
                days[9].setBackgroundColor(selected);
                createTimeSlot(9);
                break;
            case R.id.day8:
                days[10].setBackgroundColor(selected);
                createTimeSlot(10);
                break;
            case R.id.day9:
                days[11].setBackgroundColor(selected);
                createTimeSlot(11);
                break;
        }
    }

    private void setDate(int start, int weekend){
        for(int i = start;i<12;i++){
            if (i!= 5 && i != 6){
                days[i].setDate(now.plusDays(i-start+weekend).getDayOfMonth()+"-"+now.getMonthValue());
            }
        }
        for(int i = 0; i<start;i++){
            days[i].setDate(now.plusDays(i-start).getDayOfMonth()+"-"+now.getMonthValue());
        }
    }

    private void colorDays(int start){
        for(int i = 0;i<12;i++){
            if (i!= 5 && i != 6) {
                if(calendarSlots[i] == null || i < start){
                    days[i].setBackgroundColor(nothing);
                }else{
                    days[i].setBackgroundColor(normal);
                }
            }
        }
    }


    private void createTimeSlot(int day){
        if(calendarSlots[day] != null) {
            officeHours.setText("Available office hours");
            for (String appointmentTime : calendarSlots[day]) {
                TimeslotView timeSlot = new TimeslotView(this);
                timeSlot.setAppointmentTime(appointmentTime);
                timeSlot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoMakeAppointment(v, appointmentTime);
                    }
                });
                FrameLayout.LayoutParams timeSlotMarginParams = (FrameLayout.LayoutParams)slots.getLayoutParams();
                timeSlotMarginParams.setMargins(0, 0, 15, 0);
                slots.addView(timeSlot, timeSlotMarginParams);
            }
        }else{
            officeHours.setText("Sorry no office hours on this day");
        }
    }

    private static LocalDateTime toLocalDateTime(final EventDateTime edt) {
        return LocalDateTime.ofEpochSecond(
                edt.getDateTime().getValue() * 1000,
                0,
                ZoneOffset.of(edt.getTimeZone())
        );
    }

    private void setEvents(int day){
        try {
            List<Event> upcommingEvents = calendarApi.getEvents(null,todayTime,endTime); //TODO: setID
            for (Event e : upcommingEvents) {
                LocalDateTime startDay = toLocalDateTime(e.getStart());//Das geht nicht
                Duration timePassed = Duration.between(now, startDay);
                LocalDateTime endDay = toLocalDateTime(e.getEnd());//Das geht nicht
                switch (startDay.getDayOfWeek().toString()){
                    case "MONDAY":
                        setSlots(timePassed.toDays(), day, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                        break;
                    case "TUESDAY":
                        setSlots(timePassed.toDays(), day + 1, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                        break;
                    case "WEDNESDAY":
                        setSlots(timePassed.toDays(), day + 2, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                        break;
                    case "THURSDAY":
                        setSlots(timePassed.toDays(), day + 3, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                        break;
                    case "FRIDAY":
                        setSlots(timePassed.toDays(), day + 4, startDay.getHour(), startDay.getMinute(), endDay.getHour(), endDay.getMinute());
                        break;
                    default:

                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSlots(float passed, int day, int startHours, int startMinutes, int endHours, int endMinutes){
        if(passed < day){
            calendarSlots[day].add(startHours + ":" + startMinutes + "-" + endHours + ":" + endMinutes);
        }else{
            calendarSlots[day+7].add(startHours + ":" + startMinutes + "-" + endHours + ":" + endMinutes);
        }
    }

    public void gotoMakeAppointment(View view, String time) {
        Intent intent = new Intent(this, MakeAppointment.class);
        intent.putExtra("appointmentTime", time);
        startActivity(intent);
    }
}