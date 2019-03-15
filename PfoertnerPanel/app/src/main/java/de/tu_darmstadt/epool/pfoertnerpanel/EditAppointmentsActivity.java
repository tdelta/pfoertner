package de.tu_darmstadt.epool.pfoertnerpanel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;

import de.tu_darmstadt.epool.pfoertner.common.architecture.model.Appointment;

import de.tu_darmstadt.epool.pfoertner.common.PfoertnerApplication;

public class EditAppointmentsActivity extends AppCompatActivity{

    private LinearLayout scrollRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_appointments);
        scrollRequests = findViewById(R.id.scroll_requests);

        PfoertnerApplication app = PfoertnerApplication.get(this);

        long atheneId = (long) getIntent().getExtras().get("atheneId");

        if(atheneId == 0){
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

    public void displayAppointmentRequests(List<Appointment> appointmentRequests){
        if(appointmentRequests.size()==0){

        }
        for(Appointment appointment: appointmentRequests){

        }
    }
}