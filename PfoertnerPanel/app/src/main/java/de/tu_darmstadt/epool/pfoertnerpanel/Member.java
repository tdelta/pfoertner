package de.tu_darmstadt.epool.pfoertnerpanel;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedList;

public class Member {
    private Activity activity;
    private LayoutInflater inflater;
    private View group;
    private LinkedList<TextView> texts = new LinkedList<>();


    public Member(Activity activity){
        this.activity = activity;
        this.inflater = activity.getLayoutInflater();
        this.group = inflater.inflate(R.layout.member, null);
    }


    public enum Status {
        AVAILABLE, ABSENT, OUT_OF_OFFICE
    }


    public void setName(String name){
        TextView view = group.findViewById(R.id.name);
        view.setText(name);
    }


    public void setStatus(Status stat){
        TextView view = group.findViewById(R.id.status);
        switch (stat){
            case  AVAILABLE: {
                view.setText(activity.getString(R.string.available));
                view.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_green_dark));
                break;
            }
            case ABSENT: {
                view.setText(activity.getString(R.string.absent));
                view.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_orange_dark));
                break;
            }
            case OUT_OF_OFFICE: {
                view.setText(activity.getString(R.string.out_of_office));
                view.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
                break;
            }
        }
    }


    public void setOfficeHours(String[] officeHours){
        LinearLayout info = group.findViewById(R.id.info);

        for (TextView text: texts){
            info.removeView(text);
        }
        texts.clear();

        for (String officeHour: officeHours) {
            TextView txt = (TextView) inflater.inflate(R.layout.office_hours, info, false);
            texts.add(txt);
            txt.setText(officeHour);
            info.addView(txt);
        }
    }


    public void setImage(Drawable drawable){
        ImageView pic = group.findViewById(R.id.profile_picture);
        pic.setImageDrawable(drawable);
    }

    public View getView(){ return group; }

}
