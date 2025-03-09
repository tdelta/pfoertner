package de.tu_darmstadt.epool.pfoertnerpanel;

import static androidx.core.content.ContextCompat.getColor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Timeslot;
import de.tu_darmstadt.epool.pfoertnerpanel.viewmodels.ScheduleAppointmentViewModel;

/**
 * Activity that is used for making appointments.
 * Creates Dayviews and TimeSlotViews as needed
 */
public class ScheduleAppointment extends AppCompatActivity {
    private final String TAG = "NewScheduleAppointment";
    private ScheduleAppointmentViewModel viewModel;
    /**
     * Is called when activity gets created
     * Creates User Interface and listens to changes in the livedata
     * @param savedInstanceState needed if app needs to come back from background (not used by us)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_schedule_appointment);

        viewModel = new ViewModelProvider(this).get(ScheduleAppointmentViewModel.class);

        final TextView room = findViewById(R.id.schedule_room);

        PanelApplication app = PanelApplication.get(this);
        app.getRepo()
                .getOfficeRepo()
                .getOffice(app.getOffice().getId())
                .observe(this,
                        office -> {
                            if(office.getRoom()==null){
                                room.setText("Room Name Not Set");
                            } else {
                                room.setText(office.getRoom());
                            }
                        });

        int memberId = getIntent().getIntExtra("MemberId",-1);

        if (memberId < 0) {
            Log.e(TAG, "A member must be selected to show appointments.");
            return;
        }

        app
                .getRepo()
                .getMemberRepo()
                .getMember(memberId)
                .observe(this, member -> {
                    final TextView appointmentMemberName = (TextView) findViewById(R.id.appointment_member);
                    appointmentMemberName.setText("Appointment times for: " + member.getFirstName() + " " + member.getLastName());
                });

        ListView dayList = findViewById(R.id.day_list);
        DayAdapter dayAdapter = new DayAdapter(this, new ArrayList<>(30));
        dayList.setAdapter(dayAdapter);
        dayList.setOnItemClickListener((adapterView, view, i, l) -> {
            ScheduleAppointmentViewModel.DayItem dayItem = dayAdapter.getItem(i);
            if (dayItem.hasTimeslots) viewModel.selectedDay.setValue(dayItem.date);
        });

        ListView timeslotList = findViewById(R.id.timeslot_list);
        TimeslotAdapter timeslotAdapter = new TimeslotAdapter(this, new ArrayList<>(5));
        timeslotList.setAdapter(timeslotAdapter);
        timeslotList.setOnItemClickListener((adapterView, view, i, l) -> gotoMakeAppointment(timeslotAdapter.getItem(i)));

        viewModel.getNextDays(memberId, LocalDate.now())
                .observe(this, dayItems -> {
                    dayAdapter.clear();
                    dayAdapter.addAll(dayItems);

                });

        viewModel.getTimeslotsOnSelectedDay(memberId)
                .observe(this, timeslots -> {
                    timeslotAdapter.clear();
                    timeslotAdapter.addAll(timeslots);
                });
    }

    /**
     * Change current activity to MakeAppointmentActivity, while including data about the chosen
     * event in the intent
     * @param timeslot timeslot in the right sidebar
     */
    public void gotoMakeAppointment(Timeslot timeslot) {
        final Intent intent = new Intent(this, MakeAppointment.class);

        // TODO: Don't use intents, replace with Navigation Component
        intent.putExtra("appointmentStartTimeHour", timeslot.getStart().getHour());
        if(timeslot.getStart().getMinute() > 9){
            intent.putExtra("appointmentStartTimeMinutes", "" + timeslot.getStart().getMinute());
        }else{
            intent.putExtra("appointmentStartTimeMinutes", "0" + timeslot.getStart().getMinute());
        }
        intent.putExtra("appointmentEndTimeHour", timeslot.getEnd().getHour());
        if(timeslot.getEnd().getMinute() > 9){
            intent.putExtra("appointmentEndTimeMinutes", "" + timeslot.getEnd().getMinute());
        }else{
            intent.putExtra("appointmentEndTimeMinutes", "0" + timeslot.getEnd().getMinute());
        }
        intent.putExtra("Day", timeslot.getStart().getDayOfMonth());
        intent.putExtra("Month", timeslot.getStart().getMonthValue());
        intent.putExtra("Year", timeslot.getStart().getYear());
        intent.putExtra("MemberId", getIntent().getIntExtra("MemberId",-1));

        // yyyy-MM-dd HH:mm
        startActivity(intent);


        finish();
    }

    private class TimeslotAdapter extends ArrayAdapter<Timeslot> {
        public TimeslotAdapter(@NonNull Context context, @NonNull List<Timeslot> objects) {
            super(context, R.layout.timeslotview, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View timeslotView = convertView != null ?
                    convertView : LayoutInflater.from(getContext()).inflate(R.layout.timeslotview, parent, false);

            final TextView startTime = timeslotView.findViewById(R.id.start);
            final TextView endTime = timeslotView.findViewById(R.id.end);

            Timeslot timeslot = getItem(position);

            timeslotView.setBackgroundColor(getColor(R.color.colorPrimary));

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            startTime.setText(timeslot.getStart().format(dateTimeFormatter));
            endTime.setText(timeslot.getEnd().format(dateTimeFormatter));

            return timeslotView;
        }
    }

    private class DayAdapter extends ArrayAdapter<ScheduleAppointmentViewModel.DayItem> {

        public DayAdapter(@NonNull Context context, @NonNull List<ScheduleAppointmentViewModel.DayItem> objects) {
            super(context, R.layout.dayview, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View dayView = convertView != null ?
                    convertView : LayoutInflater.from(getContext()).inflate(R.layout.dayview, parent, false);

            ScheduleAppointmentViewModel.DayItem dayItem = getItem(position);
            LocalDate day = dayItem.date;
            Locale locale = getContext().getResources().getConfiguration().getLocales().get(0);

            TextView title = dayView.findViewById(R.id.title);
            title.setText(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale));

            TextView summary = dayView.findViewById(R.id.summary);
            summary.setText(day.format(DateTimeFormatter.ofPattern("dd - MM")));

            if (dayItem.isSelected) dayView.setBackgroundColor(getColor(R.color.selected));
            else if(dayItem.hasTimeslots) dayView.setBackgroundColor(getColor(R.color.colorPrimary));
            else dayView.setBackgroundColor(getColor(R.color.inactive));

            return dayView;
        }
    }
}
