package de.tu_darmstadt.epool.pfoertner.common.architecture.model;

import java.util.Date;

public interface Appointment {
    int getId();
    Date getStart();
    Date getEnd();
    String getEmail();
    String getName();
    String getMessage();
    boolean getAccepted();
    int getOfficeMemberId();
    String getAtheneId();
}
