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


/**
 * Lists days in a vertical list and highlights them differently
 * depending on the number of available timeslots
 */
public class DayView extends RelativeLayout {
    private LocalDateTime date;
    private LinkedList<Event> events;
    private final int selected = 0xFFFF4081;
    private final int nothing = 0xFFC4C4C4;
    private final int normal = 0xFF8BC34A;

    /**
     * DayView constructor
     * @param context context view containing layout
     */
    public DayView(Context context) {
        super(context);
        this.events = new LinkedList<Event>();

        init(context);
    }

    /**
     * DayView constructor
     * @param context context view containing layout
     * @param attrs
     */
    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    /**
     * DayView constructor
     * @param context context view containing layout
     * @param attrs this parameter is filled by the android inflator
     * @param defStyleAttr this parameter is filled by the android inflator
     */
    public DayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    /**
     * DayView constructor
     * @param context context view containing layout
     * @param attrs this parameter is filled by the android inflator
     * @param defStyleAttr this parameter is filled by the android inflator
     * @param defStyleRes this parameter is filled by the android inflator
     */
    public DayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    /**
     * Needs to be run while creating interface elements programmatically
     * @param context view containing layout
     */
    private void init(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dayview, this, true);

    }

    /**
     * Sets the data for a DayView
     * @param date LocalDateTime to be set
     */
    public void setDate(LocalDateTime date){
        this.date = date;
        setTitle();
        setDay();
    }

    /**
     * Sets the Title in the DayView
     */
    private void setTitle() {
        final TextView imagetest = (TextView) findViewById(R.id.title);

        imagetest.setText(date.getDayOfWeek().toString().substring(0, 1).toUpperCase() + date.getDayOfWeek().toString().substring(1,3).toLowerCase());
    }

    /**
     * Sets the Day in the DayView
     */
    private void setDay(){
        final TextView imagetest = (TextView) findViewById(R.id.summary);

        imagetest.setText(date.getDayOfMonth() + "-" + date.getMonthValue());
    }

    /**
     * Sets the color of the Dayview
     */
    public void setColor(){
        if(events.size() == 0){
            this.setBackgroundColor(nothing);
        }else{
            this.setBackgroundColor(normal);
        }
    }

    /**
     * Returns the date of the DayView
     * @return LocalDateTime with the date of the Dayview
     */
    public LocalDateTime getDate(){
        return date;
    }

    /**
     * Returns the list of events contained in the DayView
     * @return LinkedList<Event> containing the events of the DayView
     */
    public LinkedList<Event> getEvents(){
        return events;
    }

    /**
     * Add an event e to the DayView
     * @param e Event to be added to DayView
     */
    public void addEvents(Event e){
        events.add(e);
    }

}
