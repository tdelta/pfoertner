package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.LinkedList;

import de.tu_darmstadt.epool.pfoertnerpanel.R;

import static android.support.constraint.Constraints.TAG;

public class MemberFragment extends Fragment {

    private String status;
    private String[] officeHours;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.member, container, false);
    }

    public void setStatus(final String status){
        this.status = status == null ? "" : status;
    }

    public void setOfficeHours(String[] officeHours){
        this.officeHours = officeHours;
    }


    @Override
    public void onCreate (Bundle outState) {
        super.onCreate(outState);
        if (outState != null){
            status = outState.getString("status");
            officeHours = outState.getStringArray("officeHours");
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putString("status", status);
        outState.putStringArray("officeHours", officeHours);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Drawable statusIcon;
        final int bgColor;

        // set status
        try {
            switch (status) {
                case "Available":
                    statusIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_home_green_24dp);
                    bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_positive_status_bg);
                    break;

                case "Out of office":
                case "In meeting":
                    statusIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_warning_red_24dp);
                    bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_negative_status_bg);
                    break;

                default:
                    statusIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_info_yellow_24dp);
                    bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_info_status_bg);
            }

            final TextView personalStatusTextView = getActivity().findViewById(R.id.personalStatusText);
            final ImageView personalStatusImageView = getActivity().findViewById(R.id.personalStatusIcon);
            final CardView personalStatusCardView = getActivity().findViewById(R.id.personalStatusCard);

            personalStatusTextView.setText(status);
            personalStatusImageView.setImageDrawable(statusIcon);
            personalStatusCardView.setCardBackgroundColor(bgColor);
        }

        catch (final Exception exception) {
            Log.e(TAG, "Could not load resources needed to display current personal status.");
        }

        // set office times
        {
            LinearLayout info = getView().findViewById(R.id.personalOfficeTimeBoard);
            info.removeAllViews();

            for (String officeHour: officeHours) {
                TextView text = new TextView(getActivity());
                text.setTypeface(null, Typeface.BOLD);
                text.setText(officeHour);
                info.addView(text);
            }
        }
    }
}
