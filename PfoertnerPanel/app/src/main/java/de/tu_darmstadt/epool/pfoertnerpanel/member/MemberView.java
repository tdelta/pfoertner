package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.api.services.calendar.model.Event;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.R;
import de.tu_darmstadt.epool.pfoertnerpanel.helpers.Timehelpers;

import static android.support.constraint.Constraints.TAG;

public class MemberView extends CardView {
    private int memberId;
    private final static String TAG = "MemberView";

    /**
     * Constructor
     * initializes the card content including setting up glide
     * @param context view context for the layout
     * @param member Member corresponding to the memeber card
     * @param events List of Events for the Member
     */
    public MemberView(Context context, Member member, List<Event> events) {
        super(context);
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.member, this, true);

        setMemberId(member.getId());
        setName(member.getFirstName(), member.getLastName());

        Glide
                .with(getContext())
                .load(member.getPicture())
                .placeholder(ContextCompat.getDrawable(getContext(), R.drawable.ic_contact_default))
                .signature(new ObjectKey(member.getPictureMD5() == null ? "null" : member.getPictureMD5()))
                .into((ImageView) findViewById(R.id.personalProfilePicture));

        setStatus(member.getStatus());
        initOfficeHours(events);
    }

    /**
     * Extracts and encodes the first 2 events in the calender to be displayed as office hours
     * @param events List of events in the calendar
     * @return List of the first 2 events from the calendar formated as strings
     * for use as office hour display
     */
    public List<String> parseOffice(List<Event> events){
        List<String> nextOfficeHours = new LinkedList<String>();
        Timehelpers timehelper = new Timehelpers();

        int count = 0;
        for (Event e : events) {
            if (count < 2){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String startTime = timehelper.toLocalDateTime(e.getStart()).format(formatter);
                String endTime = timehelper.toLocalDateTime(e.getEnd()).format(formatter);

                nextOfficeHours.add(timehelper.toLocalDateTime(e.getStart()).getDayOfWeek().toString().substring(0, 1)
                        +timehelper.toLocalDateTime(e.getStart()).getDayOfWeek().toString().substring(1).toLowerCase()
                        +" "
                        +startTime + " - " + endTime);
            }
            count++;
        }
        return nextOfficeHours;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
        MemberButton button = findViewById(R.id.member_button);
        button.setMemberId(memberId);
    }

    public void setName(String firstName, String lastName) {
        TextView textView = findViewById(R.id.name);
        textView.setText(firstName + " " + lastName);
    }

    /**
     * Set the personal status from a given string
     * @param status Personal status as string
     */
    public void setStatus(String status) {
        final Drawable statusIcon;
        final int bgColor;

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

            final TextView personalStatusTextView = findViewById(R.id.personalStatusText);
            final ImageView personalStatusImageView = findViewById(R.id.personalStatusIcon);
            final LinearLayout personalStatusView = findViewById(R.id.personalStatus);

            personalStatusTextView.setText(status);
            personalStatusImageView.setImageDrawable(statusIcon);
            personalStatusView.setBackgroundColor(bgColor);
        } catch (final Exception exception) {
            Log.e(TAG, "Could not load resources needed to display current personal status.");
        }
    }

    /**
     * Initializes the office hours for a member
     * @param events List of events for a member
     */
    public void initOfficeHours(List<Event> events) {
        LinearLayout info = findViewById(R.id.personalOfficeTimeBoard);
        info.removeAllViews();

        List<String> officeHours = parseOffice(events);

        officeHours
                .stream()
                .forEach(officeHour -> {
                    TextView text = new TextView(getContext());
                    text.setTypeface(null, Typeface.BOLD);
                    text.setText(officeHour);
                    info.addView(text);
                });

    }

}
