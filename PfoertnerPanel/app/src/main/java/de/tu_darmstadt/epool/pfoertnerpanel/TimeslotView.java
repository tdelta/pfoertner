package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

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

    }
}
