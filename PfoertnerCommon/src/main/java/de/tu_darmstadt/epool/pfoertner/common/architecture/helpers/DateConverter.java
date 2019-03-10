package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value){
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long toTimestamp(Date date){
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static LocalDateTime fromTimestamp(String value){
        return value == null ? null : LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @TypeConverter
    public static String toTimestamp(LocalDateTime date){
        return date == null ? null : date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
