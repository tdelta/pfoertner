package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.R;

import static android.support.constraint.Constraints.TAG;

public class MemberView extends CardView {

    private int memberId;

    public MemberView(Context context, Member member) {
        super(context);
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final PfoertnerApplication app = PfoertnerApplication.get(context);

        inflater.inflate(R.layout.member, this, true);

        setMemberId(member.getId());
        setName(member.getFirstName(), member.getLastName());
        setImage(member.getPicture(app.getFilesDir()));
        setStatus(member.getMemberData().status);
        String[] work = {"Mo-Fr 8:00 - 23:00", "Sa-So 8:00 - 23:00"};
        setOfficeHours(work);
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

    public void setImage(Optional<Bitmap> image) {
        ImageView imageView = findViewById(R.id.personalProfilePicture);

        imageView.setImageDrawable(
                image
                        .map(bitmap -> (Drawable) new BitmapDrawable(getResources(), bitmap))
                        .orElse(
                                getContext().getDrawable(R.drawable.ic_contact_default)
                        ));

    }

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

    public void setOfficeHours(String[] officeHours) {
        LinearLayout info = findViewById(R.id.personalOfficeTimeBoard);
        info.removeAllViews();

        for (String officeHour : officeHours) {
            TextView text = new TextView(getContext());
            text.setTypeface(null, Typeface.BOLD);
            text.setText(officeHour);
            info.addView(text);
        }

    }
}
