package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
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
import java.util.Optional;

import de.tu_darmstadt.epool.pfoertnerpanel.R;

import static android.support.constraint.Constraints.TAG;

public class MemberFragment extends Fragment {

    private String status;
    private String[] officeHours;
    Bitmap image;

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

    public void setImage(Optional<Bitmap> image) {
        this.image = image.isPresent() ? Bitmap.createScaledBitmap(image.get(), 210, 210, false) : null;


    }

    @Override
    public void onCreate (Bundle outState) {
        super.onCreate(outState);
        if (outState != null){
            status = outState.getString("status");
            officeHours = outState.getStringArray("officeHours");
            image = outState.getParcelable("image");
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        outState.putString("status", status);
        outState.putStringArray("officeHours", officeHours);
        outState.putParcelable("image", image);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Drawable statusIcon;
        final int bgColor;

        // set image
        ImageView imageView = getActivity().findViewById(R.id.personalProfilePicture);

        if (image != null) {
            imageView.setImageDrawable(new BitmapDrawable(getResources(), image));
        } else {
            imageView.setImageDrawable(getContext().getDrawable(R.drawable.ic_contact_default));
        }

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
            final CardView personalStatusCardView = getActivity().findViewById(R.id.personalStatus);

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
