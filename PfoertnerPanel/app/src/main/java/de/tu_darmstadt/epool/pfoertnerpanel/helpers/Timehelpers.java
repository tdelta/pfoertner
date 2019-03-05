package de.tu_darmstadt.epool.pfoertnerpanel.helpers;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import java.util.TimeZone;

public class Timehelpers {

    public static LocalDateTime toLocalDateTime(final EventDateTime edt) {
        final DateTime calendarDateTime = edt.getDateTime();
        final int timeShiftInMinutes = calendarDateTime.getTimeZoneShift();

        return LocalDateTime.ofEpochSecond(
                calendarDateTime.getValue() / 1000,
                0,
                ZoneOffset.ofHoursMinutes((timeShiftInMinutes / 60)+1, timeShiftInMinutes % 60)
        );
    }

    public static boolean isItToday(final LocalDateTime today, final LocalDateTime timeOfEvent){
        final Duration timeToEvent = Duration.between(today, timeOfEvent);
        final Duration timeToNextDay = Duration.between(today, today.plusDays(1));
        if(timeToEvent.getSeconds() >= 0 &&(timeToEvent.getSeconds() < timeToNextDay.getSeconds())){
            return true;
        }else{
            return false;
        }
    }
}
