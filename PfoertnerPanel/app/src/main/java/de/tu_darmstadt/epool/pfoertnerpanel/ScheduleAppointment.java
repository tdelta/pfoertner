package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import org.threeten.bp.LocalDateTime;
import java.util.LinkedList;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment);
        days = new DayView[12];
        slots = (LinearLayout) findViewById(R.id.officehours);
        calendarSlots = new LinkedList[12];


        LinkedList<String> test = new LinkedList<String>();
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");
        test.add("1");
        test.add("2");

        calendarSlots[7] = test;
        calendarSlots[9] = test;
        calendarSlots[11] = test;

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

        now = LocalDateTime.now();
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
            for (String x : calendarSlots[day]) {
                TimeslotView timeSlot = new TimeslotView(this);
                timeSlot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "test");
                    }
                });
                slots.addView(timeSlot);
            }
        }
    }
}