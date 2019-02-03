package de.tu_darmstadt.epool.pfoertnerpanel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DayView extends RelativeLayout {
    public DayView(Context context) {
        super(context);

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

    public void setText(final String newText) {
        final TextView imagetest = (TextView) findViewById(R.id.title);

        imagetest.setText(newText);
    }

}
