package de.tu_darmstadt.epool.pfoertneradmin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

public class AppointmentRequestList extends LinearLayout{

    private final Context context;
    private LayoutInflater inflater;

    public AppointmentRequestList(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    public AppointmentRequestList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public AppointmentRequestList(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init(){
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.appointment_request_list, this, true);
    }

    public void showAppointmentRequests(List<AppointmentRequest> appointmentRequestList){
        LinearLayout scrollRequests = findViewById(R.id.scroll_requests);

        scrollRequests.removeAllViews();

        if(appointmentRequestList == null) return;

        for(AppointmentRequest appointmentRequest: appointmentRequestList){
            if(appointmentRequest.accepted) {
                View appointmentRequestView = inflater.inflate(R.layout.appointment_request, scrollRequests, false);

                StringBuilder text = new StringBuilder();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd, ", Locale.GERMANY);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY);

                OffsetDateTime startDateTime = DateTimeUtils.toInstant(appointmentRequest.start).atOffset(org.threeten.bp.ZoneOffset.UTC);
                OffsetDateTime endDateTime = DateTimeUtils.toInstant(appointmentRequest.end).atOffset(org.threeten.bp.ZoneOffset.UTC);
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

                appointmentRequestView.findViewById(R.id.accept_button).setOnClickListener(new ButtonListener(appointmentRequest.id, true));
                appointmentRequestView.findViewById(R.id.decline_button).setOnClickListener(new ButtonListener(appointmentRequest.id, false));
                scrollRequests.addView(appointmentRequestView);
            }
        }
    }

    private class ButtonListener implements OnClickListener{

        private int appointmentId;
        private boolean accept;
        private AdminApplication app;

        public ButtonListener(int appointmentId, boolean accept){
            this.appointmentId = appointmentId;
            this.accept = accept;
            app = AdminApplication.get(context);
        }

        @Override
        public void onClick(View v) {
            try {
                Member member = app.getOffice().getMemberById(app.getMemberId())
                        .orElseThrow(() -> new RuntimeException("Cant accept an appointment when no Office Member is registered"));
                member.setAppointmentRequestAccepted(app.getService(),app.getAuthentication(),appointmentId,accept);
            } catch (Throwable e){
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
