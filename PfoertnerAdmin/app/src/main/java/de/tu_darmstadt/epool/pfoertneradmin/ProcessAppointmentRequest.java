package de.tu_darmstadt.epool.pfoertneradmin;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.tu_darmstadt.epool.pfoertner.common.retrofit.AppointmentRequest;
import de.tu_darmstadt.epool.pfoertner.common.synced.Member;
import de.tu_darmstadt.epool.pfoertneradmin.calendar.LocalCalendar;

public class ProcessAppointmentRequest extends IntentService {

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();

    public ProcessAppointmentRequest(){
        super("ProcessAppointmentRequest");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Bundle extras = intent.getExtras();
        Log.d("AppointmentRequest","Handling intent");
        if(intent.getAction()=="AcceptAppointmentRequest") {
            AppointmentRequest appointmentRequest = gson.fromJson(extras.getString("data"), AppointmentRequest.class);
            AdminApplication app = AdminApplication.get(this);
            Member member = app.getOffice().getMemberById(app.getMemberId()).get();
            String email = member.getEmail();
            writeCalendarWithPermission(appointmentRequest,email);
            member.setAppointmentRequestAccepted(app.getService(),app.getAuthentication(),appointmentRequest.id,true);
        } else if (intent.getAction()=="DeclineAppointmentRequest") {
            AppointmentRequest appointmentRequest = gson.fromJson(extras.getString("data"), AppointmentRequest.class);
            Log.d("AppointmentRequest","Id: "+appointmentRequest.id);
            AdminApplication app = AdminApplication.get(this);
            Member member = app.getOffice().getMemberById(app.getMemberId()).get();
            member.setAppointmentRequestAccepted(app.getService(),app.getAuthentication(),appointmentRequest.id,false);
        }
    }

    private void writeCalendarWithPermission(AppointmentRequest appointmentRequest, String email){
        try{
            LocalCalendar.getInstance(this,email).writeEvent(
                    appointmentRequest.start,
                    appointmentRequest.end,
                    appointmentRequest.name,
                    appointmentRequest.email,
                    appointmentRequest.message
            );
        } catch (SecurityException e){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("You have to grant the permission to write into the Calendar");
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.show();
        }
    }
}
