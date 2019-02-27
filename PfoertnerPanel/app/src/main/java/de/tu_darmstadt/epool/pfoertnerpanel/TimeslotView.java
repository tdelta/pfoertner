package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.api.services.calendar.model.Event;

import org.threeten.bp.LocalDateTime;
import org.w3c.dom.Text;

import de.tu_darmstadt.epool.pfoertnerpanel.helpers.Timehelpers;


public class TimeslotView extends RelativeLayout {
    private Event event;
    private Timehelpers timehelper;

    public TimeslotView(Context context) {
        super(context);

        init(context);
    }

    public TimeslotView(Context context, Event event) {
        super(context);
        init(context);
        timehelper = new Timehelpers();
        this.event = event;
        setAppointmentTime();
    }

    public TimeslotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public TimeslotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public TimeslotView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    private void init(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.timeslotview, this, true);
        this.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));

    }

    public void setAppointmentTime() {
        final TextView startTime = (TextView) findViewById(R.id.start);
        final TextView endTime = (TextView) findViewById(R.id.end);

        if(getStartTime().getMinute() > 9){
            startTime.setText(getStartTime().getHour() + ":" + getStartTime().getMinute());
        }else{
            startTime.setText(getStartTime().getHour() + ":0" + getStartTime().getMinute());
        }

        if(getEndTime().getMinute() > 9){
            endTime.setText(getEndTime().getHour() + ":" + getEndTime().getMinute());
        }else{
            endTime.setText(getEndTime().getHour() + ":0" + getEndTime().getMinute());
        }

    }

    private LocalDateTime getStartTime(){
        return timehelper.toLocalDateTime(event.getStart());
    }

    private LocalDateTime getEndTime(){
        return timehelper.toLocalDateTime(event.getEnd());
    }

    public Event getEvent(){
        return event;
    }

}
