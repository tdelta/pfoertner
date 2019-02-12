package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class AppointmentRequest {
    @Expose public int id;
    @Expose public Date start;
    @Expose public Date end;
    @Expose public String email;
    @Expose public String name;
    @Expose public String message;
    @Expose public boolean accepted;

    public AppointmentRequest(final Date start, final Date end, final String email, final String name, final String message, final boolean accepted){
        this.start = start;
        this.end = end;
        this.email = email;
        this.name = name;
        this.message = message;
        this.accepted = accepted;
    }

    private AppointmentRequest(final int id, final Date start, final Date end, final String email, final String name, final String message, final boolean accepted){
        this(start,end,email,name,message,accepted);
        this.id = id;
    }

    public AppointmentRequest deepCopy(){
        return new AppointmentRequest(
                id,
                start,
                end,
                email,
                name,
                message,
                accepted
        );
    }

    public boolean equals(AppointmentRequest other){
        return
                other != null
                && id == other.id
                && start.equals(other.start)
                && end.equals(other.end)
                && email.equals(other.email)
                && name.equals(other.name)
                && message.equals(other.message)
                && accepted == other.accepted;
    }
}
