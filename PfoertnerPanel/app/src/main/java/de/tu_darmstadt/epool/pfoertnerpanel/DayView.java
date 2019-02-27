package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.threeten.bp.LocalDateTime;
import com.google.api.services.calendar.model.Event;

import java.util.LinkedList;


public class DayView extends RelativeLayout {
    private LocalDateTime date;
    private LinkedList<Event> events;
    private final int selected = 0xFFFF4081;
    private final int nothing = 0xFF808080;
    private final int normal = 0xFF8BC34A;

    public DayView(Context context) {
        super(context);
        this.events = new LinkedList<Event>();

        init(context);
    }

    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public DayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public DayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    private void init(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dayview, this, true);

    }

    public void setDate(LocalDateTime date){
        this.date = date;
        setTitle();
        setDay();
    }

    private void setTitle() {
        final TextView imagetest = (TextView) findViewById(R.id.title);

        imagetest.setText(date.getDayOfWeek().toString().substring(0, 1).toUpperCase() + date.getDayOfWeek().toString().substring(1,3).toLowerCase());
    }

    private void setDay(){
        final TextView imagetest = (TextView) findViewById(R.id.summary);

        imagetest.setText(date.getDayOfMonth() + "-" + date.getMonthValue());
    }

    public void setColor(){
        if(events.size() == 0){
            this.setBackgroundColor(nothing);
        }else{
            this.setBackgroundColor(normal);
        }
    }

    public LocalDateTime getDate(){
        return date;
    }

    public LinkedList<Event> getEvents(){
        return events;
    }

    public void addEvents(Event e){
        events.add(e);
    }

}
