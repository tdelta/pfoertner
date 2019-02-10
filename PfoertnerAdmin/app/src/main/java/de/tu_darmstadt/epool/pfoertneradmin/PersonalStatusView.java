package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

public class PersonalStatusView extends LinearLayout {
    private final static String TAG = "PersonalStatusView";

    public PersonalStatusView(@NonNull Context context) {
        super(context);

        init(context);
    }

    public PersonalStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public PersonalStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(final Context context) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.personal_status_view, this, true);
    }

    public void setStatus(final String status) {
        final String statusText;
        final Drawable statusIcon;
        final int bgColor;
        final int tapTextColor;

        try {
            switch (status) {
                case "Available":
                    statusText = "You're available";
                    statusIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_home_green_60dp);
                    bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_positive_status_bg);
                    tapTextColor = ContextCompat.getColor(getContext(), R.color.pfoertner_positive_status_taptext);
                    break;

                default:
                    statusText = status;
                    statusIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_warning_red_60dp);
                    bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_negative_status_bg);
                    tapTextColor = ContextCompat.getColor(getContext(), R.color.pfoertner_negative_status_taptext);
            }

            final TextView personalStatusTextView = (TextView) findViewById(R.id.personalStatusText);
            final ImageView personalStatusImageView = (ImageView) findViewById(R.id.personalStatusIcon);
            final CardView personalStatusCardView = (CardView) findViewById(R.id.personalStatusCard);
            final TextView personalStatusTapText = (TextView) findViewById(R.id.personalStatusTapText);

            personalStatusTextView.setText(statusText);
            personalStatusImageView.setImageDrawable(statusIcon);
            personalStatusCardView.setCardBackgroundColor(bgColor);
            personalStatusTapText.setTextColor(tapTextColor);
        }

        catch (final Exception exception) {
            Log.e(TAG, "Could not load resources needed to display current personal status.");
        }
    }
}
