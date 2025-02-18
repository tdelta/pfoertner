package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * An image view whose whole purpose is to display an image as a square
 */
public class MemberIcon extends AppCompatImageView {

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

    /**
     * This is called to find out how big a view should be.
     * The parent supplies constraint information in the width and height parameters. 
     * Sets the height to the same value as the width
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent
     */
    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        int measuredHeight = getMeasuredHeight();

        setMeasuredDimension(measuredHeight, measuredHeight);

    }

}
