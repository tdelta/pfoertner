package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class TimeslotView extends RelativeLayout {
    public TimeslotView(Context context) {
        super(context);

        init(context);
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

    public void setAppointmentTime(final String newText) {
        final TextView startTime = (TextView) findViewById(R.id.start);
        final TextView endTime = (TextView) findViewById(R.id.end);

        String[] times = newText.split(" - ");

        startTime.setText(times[0]);
        endTime.setText(times[1]);
    }


}
