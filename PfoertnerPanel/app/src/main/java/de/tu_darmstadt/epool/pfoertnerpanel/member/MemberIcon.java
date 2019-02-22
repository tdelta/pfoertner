package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MemberIcon extends android.support.v7.widget.AppCompatImageView {

    public MemberIcon(final Context context) {
        super(context);
    }

    public MemberIcon(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public MemberIcon(final Context context, final AttributeSet attrs,
                final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        int measuredHeight = getMeasuredHeight();

        setMeasuredDimension(measuredHeight, measuredHeight);

    }

}
