package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class AppointmentRequest {
    @Expose public Date start;
    @Expose public Date end;
    @Expose public String email;
    @Expose public String name;
    @Expose public String message;

    public AppointmentRequest(final Date start, final Date end, final String email, final String name, final String message){
        this.start = start;
        this.end = end;
        this.email = email;
        this.name = name;
        this.message = message;
    }

    public AppointmentRequest deepCopy(){
        return new AppointmentRequest(
                start,
                end,
                email,
                name,
                message
        );
    }
}
