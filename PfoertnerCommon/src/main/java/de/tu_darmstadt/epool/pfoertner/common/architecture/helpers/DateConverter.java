package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;

import androidx.room.TypeConverter;
import android.util.Log;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Date;

/**
 * Used by room to convert objects into primitive types that can be inserted into the local database
 */
public class DateConverter {
    /**
     * Converts a unix timestamp to a java.util.Date object
     * @param value Unix timestamp in ms
     * @return Date object specifying the same date and time as the input
     */
    @TypeConverter
    public static Date fromTimestamp(Long value){
        return value == null ? null : new Date(value);
    }

    /**
     * Converts a java.util.Date object into a unix timestamp
     * @param date Data object
     * @return Unix timestamp in ms specifying the same date and time as the input
     */
    @TypeConverter
    public static Long toTimestamp(Date date){
        return date == null ? null : date.getTime();
    }

    /**
     * Converts a String representation of a date and time in the iso_local_date_time format ('2019-10-03T09:30:00') into a LocalDateTime object
     * @param value String representation
     * @return LocalDateTime object
     */
    @TypeConverter
    public static LocalDateTime fromTimestamp(String value){
        return value == null ? null : LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Converts a LocalDateTime object into a String representation in the iso_local_date_time format ('2019-10-03T09:30:00')
     * @param date LocalDateTime object
     * @return String representation
     */
    @TypeConverter
    public static String toTimestamp(LocalDateTime date){
        return date == null ? null : date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
