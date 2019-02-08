package de.tu_darmstadt.epool.pfoertnerpanel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.threeten.bp.LocalDateTime;

public class ScheduleAppointment extends AppCompatActivity {
    LocalDateTime now;
    String TAG = "Schedule ";
    DayView days[];
    final int selected = 0xFFEB3BFF;
    final int nothing = 0xbdbdbdff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment);
        days = new DayView[12];


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
                colorDaysPassed(0);
                setDate(0,0);
                break;
            case "TUESDAY":
                colorDaysPassed(1);
                setDate(1,0);
                break;
            case "WEDNESDAY":
                colorDaysPassed(2);
                setDate(2,0);
                break;
            case "THURSDAY":
                setDate(3,0);
                colorDaysPassed(3);
                break;
            case "FRIDAY":
                setDate(4,0);
                colorDaysPassed(4);
                break;
            default:
                colorDaysPassed(0);
                switch (now.getDayOfWeek().toString()){
                    case "SATURDAY":
                        setDate(0,2);
                        break;
                    case "SUNDAY":
                        setDate(0,1);
                        break;
                }
                break;
        }


    }

    public void week0day0(View view){
        days[0].setBackgroundColor(selected);
        Log.d(TAG, "week0day0");
        LinearLayout slots = (LinearLayout) findViewById(R.id.officehours);
        TimeslotView test = new TimeslotView(this);
        TimeslotView test2 = new TimeslotView(this);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        slots.removeAllViews();
//        slots.addView(new TimeslotView(this));
        slots.addView(test,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        slots.addView(test2,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    }

    public void week0day1(View view){
        Log.d(TAG, "week0day1");
    }

    public void week0day2(View view){
        Log.d(TAG, "week0day2");
    }

    public void week0day3(View view){
        Log.d(TAG, "week0day3");
    }

    public void week0day4(View view){
        Log.d(TAG, "week0day4");
    }

    public void week1day0(View view){
        Log.d(TAG, "week1day0");
    }

    public void week1day1(View view){
        Log.d(TAG, "week1day1");
    }

    public void week1day2(View view){
        Log.d(TAG, "week1day2");
    }

    public void week1day3(View view){
        Log.d(TAG, "week1day3");
    }

    public void week1day4(View view){
        Log.d(TAG, "week1day4");
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

    private void colorDaysPassed(int start){
        for(int i = 0;i<start;i++){
            days[i].setBackgroundColor(nothing);
        }
    }



}
