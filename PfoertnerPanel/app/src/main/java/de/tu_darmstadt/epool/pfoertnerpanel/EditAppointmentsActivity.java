package de.tu_darmstadt.epool.pfoertnerpanel;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Displays all appointments associated with an Athene-Card
 * and offers a delete operations
 */
public class EditAppointmentsActivity extends AppCompatActivity {

    private LinearLayout scrollRequests;
    private PfoertnerApplication app;
    private static final String TAG = "EditAppointmentsActivity";

    private CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Create the activity
     * @param savedInstanceState not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_appointments);
        scrollRequests = findViewById(R.id.scroll_requests);

        app = PfoertnerApplication.get(this);

        String atheneId = (String) getIntent().getExtras().get("atheneId");

        if(atheneId == null){
            // Dont display appointment requests
            displayAppointmentRequests(Arrays.asList(new Appointment[0]));
        }

        else {
            app
                    .getRepo()
                    .getAppointmentRepository()
                    .getAppointmentsForAtheneId(atheneId)
                    .observe(this,this::displayAppointmentRequests);
        }
    }

    /**
     * Display all appointments
     * @param appointmentRequests list off appointments
     *
     */
    public void displayAppointmentRequests(List<Appointment> appointmentRequests){
        scrollRequests.removeAllViews();

        if(appointmentRequests.size()==0){
            final View noAppointmentsView = getLayoutInflater().inflate(R.layout.text_card,scrollRequests,true);
            TextView noAppointmentsText = noAppointmentsView.findViewById(R.id.cardText);
            noAppointmentsText.setText("No appointments were registered with this card");
        }

        for(Appointment appointmentRequest: appointmentRequests){
            if(!appointmentRequest.getAccepted()){
                final View appointmentView = getLayoutInflater().inflate(R.layout.appointment_request,scrollRequests,false);

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

                Log.d(TAG,text.toString());

                TextView appointmentText = appointmentView.findViewById(R.id.text);
                appointmentText.setText(text.toString());

                FrameLayout deleteButton = appointmentView.findViewById(R.id.delete_button);
                deleteButton.setOnClickListener(createDeleteAppointmentListener(appointmentRequest.getId()));

                scrollRequests.addView(appointmentView);
            }
        }
    }

    /**
     * Returns an onclick method listener for an appointment
     * @param appointmentId the id of the appointment to delete
     * @return an onclick method listener that can be used for a view
     */
    private View.OnClickListener createDeleteAppointmentListener(int appointmentId){
        return view -> {
            disposables.add(app
                    .getRepo()
                    .getAppointmentRepository()
                    .removeAppointment(appointmentId)
                    .subscribe(
                            () -> {},
                            throwable -> Log.e(TAG,"Error deleting appointment "+appointmentId,throwable)
                    )
            );
        };
    }

    /**
     * Dispose RXJava disposables
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        disposables.dispose();
    }
}
