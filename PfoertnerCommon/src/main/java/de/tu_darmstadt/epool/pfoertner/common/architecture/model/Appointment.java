package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

import java.util.Date;

/**
 * Appointment requests created at the door panel that can also be accepted appointments
 */
public interface Appointment {
    /**
     * Unique id
     */
    int getId();

    /**
     * Start date of the appointment request
     */
    Date getStart();

    /**
     * End date of the appointment request
     */
    Date getEnd();

    /**
     * Email of the person that requested the appointment
     */
    String getEmail();

    /**
     * Name of the person that requested the appointment
     */
    String getName();

    /**
     * Message of the appointment request
     */
    String getMessage();

    /**
     * True if the office member accepted the appointment
     */
    boolean getAccepted();

    /**
     * Foreign key, each appointment is requested for a specific office member
     */
    int getOfficeMemberId();

    /**
     * Id of the athene card that can be registered when an appointment is requested.
     * null if no athene card was registered
     */
    String getAtheneId();
}
