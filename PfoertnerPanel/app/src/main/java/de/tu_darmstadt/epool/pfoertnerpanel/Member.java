package de.tu_darmstadt.epool.pfoertnerpanel;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;

public class Member extends Fragment {
    //private LinkedList<TextView> texts = new LinkedList<>();
    private String name = "Max Mustermann";
    private Status status = Status.AVAILABLE;
    private String[] officeHours = {};

    public enum Status {
        AVAILABLE, ABSENT, OUT_OF_OFFICE
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.member, container, false);
    }

    public void setName(String name){
        this.name = name;

    }


    public void setStatus(Status status){
        this.status = status;
    }

    /*
    public void setImage(Drawable drawable){
        ImageView pic = getView().findViewById(R.id.profile_picture);
        pic.setImageDrawable(drawable);
    }
*/
    public void setOfficeHours(String[] officeHours){
        this.officeHours = officeHours;
    }


    @Override
    public void onCreate (Bundle outState) {
        super.onCreate(outState);
        if (outState != null){
            name = outState.getString("name");
            status = (Status) outState.getSerializable("status");
            officeHours = outState.getStringArray("ofiiiceHours");
    }

    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putString("name", name);
        outState.putSerializable("status", status);
        outState.putStringArray("officeHours", officeHours);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set name
        {
            TextView view = getView().findViewById(R.id.name);
            view.setText(name);
        }
        // set status
        {
            TextView view = getView().findViewById(R.id.status);
            switch (status) {
                case AVAILABLE: {
                    view.setText(getActivity().getString(R.string.available));
                    view.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark));
                    break;
                }
                case ABSENT: {
                    view.setText(getActivity().getString(R.string.absent));
                    view.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                    break;
                }
                case OUT_OF_OFFICE: {
                    view.setText(getActivity().getString(R.string.out_of_office));
                    view.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));
                    break;
                }
            }
        }
        // set office times
        {
            LinearLayout info = getView().findViewById(R.id.office_time_board);
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
