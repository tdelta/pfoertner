package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Available time slots for appointment requests.
 * These are retrieved from Google Calendar by the server and displayed on the panel.
 */
public interface Timeslot {
    /**
     * Unique id
     * Primary key is a string, so we can copy it from google, ensuring a direct mapping
     */
    String getId();

    /**
     * Id of the office member that this time slot belongs to
     */
    int getOfficeMemberId();
    /**
     * Start date of the time slot
     */
    LocalDateTime getStart();

    /**
     * End date of the time slot
     */
    LocalDateTime getEnd();
}
