package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Optional;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertnerpanel.R;

public class MemberView extends LinearLayout {

    private MemberFragment fragment;

    private final int memberId;

    public MemberView(Context context, Member member) {
        super(context);
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final PfoertnerApplication app = PfoertnerApplication.get(context);

        inflater.inflate(R.layout.member_main, this, true);

        initFragment(member);
        setName(member.getFirstName(), member.getLastName());
        setImage(member.getPicture(app.getFilesDir()));
        setStatus(member.getMemberData().status);
        memberId = member.getId();
    }

    public int getMemberId(){
        return memberId;
    }

    public void initFragment(Member member) {
        fragment = new MemberFragment();
        String[] work = {"Mo-Fr 8:00 - 23:00", "Sa-So 8:00 - 23:00"};

        fragment.setStatus(member.getMemberData().status);
        fragment.setOfficeHours(work);
    }

    public void setName(String firstName, String lastName) {
        TextView textView = findViewById(R.id.name);
        textView.setText(firstName + " " + lastName);
    }

    public void setImage(Optional<Bitmap> image) {
        ImageView imageView = findViewById(R.id.profile_picture);

        imageView.setImageDrawable(
                image
                        .map(bitmap -> (Drawable) new BitmapDrawable(getResources(), bitmap))
                        .orElse(
                                getContext().getDrawable(R.drawable.ic_contact_default)
                        ));

    }

    public void setStatus(String status) {
        TextView textView = findViewById(R.id.status);
        textView.setText(status);
        final int bgColor;
        status = status == null ? "" : status;

        switch (status) {
            case "Available":
                bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_positive_status_taptext);
                break;

            case "Out of office":
            case "In meeting":
                bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_negative_status_taptext);
                break;

            default:
                bgColor = ContextCompat.getColor(getContext(), R.color.pfoertner_info_status_taptext);
        }
        textView.setTextColor(bgColor);
    }

    public MemberFragment getFragment() {
        return fragment;
    }
}
