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

import java.io.IOException;
import java.util.LinkedList;

public class MemberFragment extends Fragment {
    //private LinkedList<TextView> texts = new LinkedList<>();
    private String name = "Max Mustermann";
    private String status = "";
    private Drawable picture;
    private String[] officeHours = {};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.member, container, false);
    }

    public void setName(String name){
        this.name = name;

    }

    public void setStatus(final String status){
        this.status = status;
    }

    public void setPicture(final Drawable drawable){
        this.picture = drawable;
    }

    public void setOfficeHours(String[] officeHours){
        this.officeHours = officeHours;
    }


    @Override
    public void onCreate (Bundle outState) {
        super.onCreate(outState);
        if (outState != null){
            name = outState.getString("name");
            status = outState.getString("status");
            officeHours = outState.getStringArray("officeHours");
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putString("name", name);
        outState.putString("status", status);
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

            if (status != null) {
                switch (status) {
                    case "Available": {
                        view.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark));
                        break;
                    }
                    case "Absent": {
                        view.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_orange_dark));
                        break;
                    }
                    case "Out of office": {
                        view.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));
                        break;
                    }
                }

                view.setText(status);
            }

            else {
                view.setText("");
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
        // set picture
        final ImageView pic = getView().findViewById(R.id.profile_picture);
        if (this.picture != null) {
            pic.setImageDrawable(this.picture);
        }

        else {
            try {
                final Drawable defaultPic = getContext().getDrawable(R.drawable.ic_contact_default);

                if (defaultPic != null) {
                    pic.setImageDrawable(defaultPic);
                }
            }

            catch (final NullPointerException npe) {
                npe.printStackTrace();
            }
        }
    }
}
