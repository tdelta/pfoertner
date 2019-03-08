package de.tu_darmstadt.epool.pfoertnerpanel.member;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

import de.tu_darmstadt.epool.pfoertnerpanel.PanelApplication;
import de.tu_darmstadt.epool.pfoertnerpanel.R;
import de.tu_darmstadt.epool.pfoertnerpanel.models.MemberCalendarInfo;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.support.constraint.Constraints.TAG;

public class MemberOfficeHourFragment extends Fragment {
    private int memberId;
    private ArrayList<String> officeHours;

    @Override
    public void setArguments (Bundle args) {
        memberId = args.getInt("memberId", -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            memberId = savedInstanceState.getInt("memberId", -1);
            officeHours = savedInstanceState.getStringArrayList("officeHours");
            refreshOfficeHours();
        }
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.member_officehour, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("memberId", memberId);
        outState.putStringArrayList("officeHours", officeHours);
    }


    private void init(int memberId) {
        final PanelApplication app = PanelApplication.get(getActivity());
        app
                .getPanelRepo()
                .getMemberCalendarInfoRepo()
                .getCalendarInfoByMemberId(memberId)
                .observe(this, calendarInfo -> {
                    if (calendarInfo != null && calendarInfo.getCalendarId() != null) {
                        final DateTime start = new DateTime(System.currentTimeMillis());
                        final DateTime end = new DateTime(System.currentTimeMillis() + 86400000L *28);

                        getEvents(calendarInfo, start, end)
                                .subscribe(
                                        events -> {

                                            System.out.println("OfficeHours received");
                                            clearOfficeHours();

                                            events
                                                    .stream()
                                                    .map(Event::getDescription)
                                                    .forEach(this::addOfficeHours);
                                            },
                                        throwable -> Log.e(TAG, "Failed to fetch events.", throwable)
                                );
                    }
                });
    }

    private void clearOfficeHours() {
        LinearLayout info = getView().findViewById(R.id.personalOfficeTimeBoard);
        info.removeAllViews();
    }

    private void addOfficeHours(String description) {
        officeHours.add(description);
        LinearLayout info = getView().findViewById(R.id.personalOfficeTimeBoard);
        TextView text = new TextView(getContext());
        text.setTypeface(null, Typeface.BOLD);
        text.setText(description + "<- neues event");
        info.addView(text);
    }

    private void refreshOfficeHours() {
        clearOfficeHours();
        officeHours
                .stream()
                .forEach(this::addOfficeHours);
    }

    private Single<List<Event>> getEvents(final MemberCalendarInfo calendarInfo, final DateTime start, final DateTime end) {
        final PanelApplication app = PanelApplication.get(getActivity());

        return app
                .getCalendarApi()
                .getCredential(calendarInfo.getOAuthToken())
                .flatMap(
                        credentials -> app
                                .getCalendarApi()
                                .getEvents(calendarInfo.getCalendarId(), credentials, start, end)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
