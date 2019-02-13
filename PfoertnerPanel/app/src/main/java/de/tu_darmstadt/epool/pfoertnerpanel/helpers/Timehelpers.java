package de.tu_darmstadt.epool.pfoertnerpanel.helpers;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

public class Timehelpers {

    public static LocalDateTime toLocalDateTime(final EventDateTime edt) {
        final DateTime calendarDateTime = edt.getDateTime();
        final int timeShiftInMinutes = calendarDateTime.getTimeZoneShift();

        return LocalDateTime.ofEpochSecond(
                calendarDateTime.getValue() / 1000,
                0,
                ZoneOffset.ofHoursMinutes(timeShiftInMinutes / 60, timeShiftInMinutes % 60)
        );
    }
}
