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

/**
 * Fragment class that contains all appointment requests
 * or an option to access the calendar if the permission wasn't given yet
 */
public class AppointmentRequestList extends Fragment{

    private LayoutInflater inflater;
    private static int WRITE_CALENDAR_PERMISSION_REQUEST = 0;

    private static final String TAG = "AppointmentRequestList";

    private Appointment appointmentRequestToWrite;
    private String emailToWrite;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState){
        this.inflater = layoutInflater;
        return inflater.inflate(R.layout.appointment_request_list,container,true);
    }

    /**
     * Show appoitnments in the UI and an options to accept or deny
     * @param appointmentRequestList a list of appoitnments that should be displayed
     */
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

    /**
     * Button Listener for Appointment requests
     */
    private class ButtonListener implements View.OnClickListener {

        /**
         * Appointment associated with the listener
         */
        private Appointment appointmentRequest;
        /**
         * boolean on wether to accept this appointment or to deny it
         */
        private boolean accept;
        /**
         * shared application state
         */
        private AdminApplication app;

        public ButtonListener(Appointment appointmentRequest, boolean accept){
            this.appointmentRequest = appointmentRequest;
            this.accept = accept;
            app = AdminApplication.get(getContext());
        }

        /**
         * Accept or deny a request depending on the state of the accept boolean
         * @param view the view that invoked the method
         */
        @Override
        public void onClick(View v) {
            try {
                if(accept){
                    app
                            .getRepo()
                            .getAppointmentRepository()
                            .setAccepted(appointmentRequest.getId(),accept)
                            .subscribe(
                                    () -> Log.d(TAG,"Successfully send accept appointment to server"),
                                    throwable -> Log.e(TAG,"Could not send accept appointment to server")
                            );

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
                } else {
                    app
                            .getRepo()
                            .getAppointmentRepository()
                            .removeAppointment(appointmentRequest.getId())
                            .subscribe(
                                    () -> Log.d(TAG,"Successfully removed appointment request "+appointmentRequest.getId()),
                                    throwable -> Log.e(TAG,"Could not remove appointment request "+appointmentRequest.getId(),throwable)
                            );
                }
            } catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    /**
     * write an accepted appointment into the google calendar
     * @param appointmentRequest the appointment request
     * @param email the email of the member
     */
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

    /**
     * checks if it has the required permission to write in the calendar and requests it if not
     * @param appointmentRequest the appointment request
     * @param email the email of the member
     */
    private void writeCalendarEvent(Appointment appointmentRequest, String email){
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

    /**
     * When the permission request returns it executes this method.
     * If the permission was granted write the appointment into the calendar.
     * @param requestCode the requestCode of the request
     * @param permissions not used 
     * @param grantResults contains the information if the permission got granted
     */
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
