package de.tu_darmstadt.epool.pfoertneradmin;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;
import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.LocalCalendar;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

public class AppointmentRequestList extends Fragment{

    private LayoutInflater inflater;
    private static int WRITE_CALENDAR_PERMISSION_REQUEST = 0;

    private Appointment appointmentRequestToWrite;
    private String emailToWrite;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState){
        this.inflater = layoutInflater;
        return inflater.inflate(R.layout.appointment_request_list,container,true);
    }

    public void showAppointmentRequests(List<Appointment> appointmentRequestList){
        LinearLayout scrollRequests = getView().findViewById(R.id.scroll_requests);

        scrollRequests.removeAllViews();

        if(appointmentRequestList == null) return;

        for(Appointment appointmentRequest: appointmentRequestList){
            if(!appointmentRequest.getAccepted()) {
                View appointmentRequestView = inflater.inflate(R.layout.appointment_request, scrollRequests, false);

                StringBuilder text = new StringBuilder();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd, ", Locale.GERMANY);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY);

                OffsetDateTime startDateTime = DateTimeUtils.toInstant(appointmentRequest.getStart()).atOffset(ZoneOffset.of("+01:00"));
                OffsetDateTime endDateTime = DateTimeUtils.toInstant(appointmentRequest.getEnd()).atOffset(ZoneOffset.of("+01:00"));
                text.append(dateFormatter.format(startDateTime));
                text.append(timeFormatter.format(startDateTime));
                text.append(" - ");
                text.append(timeFormatter.format(endDateTime));
                text.append("\n");
                text.append(appointmentRequest.getName());
                text.append(": ");
                text.append(appointmentRequest.getMessage());

                TextView textView = appointmentRequestView.findViewById(R.id.text);
                textView.setText(text.toString());

                appointmentRequestView.findViewById(R.id.accept_button).setOnClickListener(new ButtonListener(appointmentRequest, true));
                appointmentRequestView.findViewById(R.id.decline_button).setOnClickListener(new ButtonListener(appointmentRequest, false));
                scrollRequests.addView(appointmentRequestView);
            }
        }
    }

    private class ButtonListener implements View.OnClickListener {

        private Appointment appointmentRequest;
        private boolean accept;
        private AdminApplication app;

        public ButtonListener(Appointment appointmentRequest, boolean accept){
            this.appointmentRequest = appointmentRequest;
            this.accept = accept;
            app = AdminApplication.get(getContext());
        }

        @Override
        public void onClick(View v) {
            try {
                app
                        .getRepo()
                        .getAppointmentRepository()
                        .setAccepted(appointmentRequest.getId(),accept);

                if(accept){
                    LiveData<Member> memberLiveData = app.
                            getRepo()
                            .getMemberRepo()
                            .getMember(app.getMemberId());

                    memberLiveData.observe(AppointmentRequestList.this,
                        new Observer<Member>() {
                            @Override
                            public void onChanged(@Nullable Member member) {
                                memberLiveData.removeObserver(this);
                                Log.d("AppointmentRequest","Writing calendar for email: "+member.getEmail());
                                writeCalendarEvent(
                                        appointmentRequest,
                                        member.getEmail()
                                );
                            }
                        }
                    );
                }
            } catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
    private void writeCalendarWithPermission(Appointment appointmentRequest,String email){
        try{
            LocalCalendar.getInstance(getContext(),email).writeEvent(
                    appointmentRequest.getStart(),
                    appointmentRequest.getEnd(),
                    appointmentRequest.getName(),
                    appointmentRequest.getEmail(),
                    appointmentRequest.getMessage()
            );
        } catch (SecurityException e){
            // This point in the code should never be reached
            ErrorInfoDialog.show(getContext(), "You have to grant the permission to write into the calendar", aVoid ->writeCalendarEvent(appointmentRequest,email),true);
        }
    }

    private void writeCalendarEvent(Appointment appointmentRequest,String email){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR},
                    WRITE_CALENDAR_PERMISSION_REQUEST);
            this.appointmentRequestToWrite = appointmentRequest;
            this.emailToWrite = email;
        } else {
            writeCalendarWithPermission(appointmentRequest,email);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        if(requestCode == WRITE_CALENDAR_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (appointmentRequestToWrite != null) {
                    writeCalendarWithPermission(appointmentRequestToWrite,emailToWrite);
                    appointmentRequestToWrite = null;
                }
            } else if (appointmentRequestToWrite != null) {
                // The permission was not granted, but the user requested writing into the calendar
                ErrorInfoDialog.show(getContext(), "You have to grant the permission to write into the calendar", aVoid -> writeCalendarEvent(appointmentRequestToWrite,emailToWrite),true);
            }
        }
    }
}