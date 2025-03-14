package de.tu_darmstadt.epool.pfoertner.common.retrofit;

import com.google.gson.annotations.Expose;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Only fields with the Expose annotation will be sent to the server
// Other fields will only be persisted in local memory

/**
 * Old member data marshalling class used to send and receive updates about an office member.
 * Use instead: de.tu_darmstadt.epool.pfoertner.common.architecture.model.Member
 */
@Deprecated
public class MemberData {
    @Expose public int id;
    @Expose public String lastName;
    @Expose public String firstName;
    @Expose public String pictureMD5;
    @Expose public String status;
    @Expose public List<AppointmentRequest> appointmentRequests;
    public String calendarId;
    public String email;

    public MemberData (final int id, final String lastName, final String firstName, final String pictureMD5, final String status){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pictureMD5 = pictureMD5;
        this.status = status;
    }

    public MemberData (final int id,
                       final String lastName,
                       final String firstName,
                       final String pictureMD5,
                       final String status,
                       final List<AppointmentRequest> appointmentRequests,
                       final String calendarId,
                       final String email
    ){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pictureMD5 = pictureMD5;
        this.status = status;
        this.appointmentRequests = appointmentRequests;
        this.calendarId = calendarId;
        this.email = email;
    }

    public MemberData deepCopy() {
        return new MemberData(
                id,
                lastName, // deep copying strings is not necessary, since they are immutable
                firstName,
                pictureMD5,
                status,
                appointmentRequests == null ?
                          null
                        : appointmentRequests.stream().map(AppointmentRequest::deepCopy).collect(Collectors.toList()),
                calendarId,
                email
        );
    }
}
