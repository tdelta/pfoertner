package de.tu_darmstadt.epool.pfoertneradmin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertner.common.ErrorInfoDialog;
import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
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

    private AppointmentRequest appointmentRequestToWrite;
    private String emailToWrite;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState){
        this.inflater = layoutInflater;
        return inflater.inflate(R.layout.appointment_request_list,container,true);
    }

    public void showAppointmentRequests(List<AppointmentRequest> appointmentRequestList){
        LinearLayout scrollRequests = getView().findViewById(R.id.scroll_requests);

        scrollRequests.removeAllViews();

        if(appointmentRequestList == null) return;

        for(AppointmentRequest appointmentRequest: appointmentRequestList){
            if(!appointmentRequest.accepted) {
                View appointmentRequestView = inflater.inflate(R.layout.appointment_request, scrollRequests, false);

                StringBuilder text = new StringBuilder();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd, ", Locale.GERMANY);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY);

                OffsetDateTime startDateTime = DateTimeUtils.toInstant(appointmentRequest.start).atOffset(ZoneOffset.of("+01:00"));
                OffsetDateTime endDateTime = DateTimeUtils.toInstant(appointmentRequest.end).atOffset(ZoneOffset.of("+01:00"));
                text.append(dateFormatter.format(startDateTime));
                text.append(timeFormatter.format(startDateTime));
                text.append(" - ");
                text.append(timeFormatter.format(endDateTime));
                text.append("\n");
                text.append(appointmentRequest.name);
                text.append(": ");
                text.append(appointmentRequest.message);

                TextView textView = appointmentRequestView.findViewById(R.id.text);
                textView.setText(text.toString());

                appointmentRequestView.findViewById(R.id.accept_button).setOnClickListener(new ButtonListener(appointmentRequest, true));
                appointmentRequestView.findViewById(R.id.decline_button).setOnClickListener(new ButtonListener(appointmentRequest, false));
                scrollRequests.addView(appointmentRequestView);
            }
        }
    }

    private class ButtonListener implements View.OnClickListener {

        private AppointmentRequest appointmentRequest;
        private boolean accept;
        private AdminApplication app;

        public ButtonListener(AppointmentRequest appointmentRequest, boolean accept){
            this.appointmentRequest = appointmentRequest;
            this.accept = accept;
            app = AdminApplication.get(getContext());
        }

        @Override
        public void onClick(View v) {
            try {
                Member member = app.getOffice().getMemberById(app.getMemberId())
                        .orElseThrow(() -> new RuntimeException("Cant accept an appointment when no Office Member is registered"));
                member.setAppointmentRequestAccepted(app.getService(),app.getAuthentication(),appointmentRequest.id,accept);
                String email = member.getEmail();
                if(accept){
                    writeCalendarEvent(
                            appointmentRequest,
                            email
                    );
                }
            } catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
    private void writeCalendarWithPermission(AppointmentRequest appointmentRequest,String email){
        try{
            LocalCalendar.getInstance(getContext(),email).writeEvent(
                    appointmentRequest.start,
                    appointmentRequest.end,
                    appointmentRequest.name,
                    appointmentRequest.email,
                    appointmentRequest.message
            );
        } catch (SecurityException e){
            // This point in the code should never be reached
            ErrorInfoDialog.show(getContext(), "You have to grant the permission to write into the calendar", aVoid ->writeCalendarEvent(appointmentRequest,email),true);
        }
    }

    private void writeCalendarEvent(AppointmentRequest appointmentRequest,String email){
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