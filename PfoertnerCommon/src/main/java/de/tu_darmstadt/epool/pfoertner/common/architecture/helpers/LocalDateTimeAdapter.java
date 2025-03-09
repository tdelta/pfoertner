package de.tu_darmstadt.epool.pfoertner.common.architecture.helpers;

import androidx.annotation.NonNull;

import com.google.api.client.json.JsonString;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.annotation.Nullable;

import retrofit2.Converter;
import retrofit2.Retrofit;

public class LocalDateTimeAdapter implements JsonDeserializer<LocalDateTime>, Converter<LocalDateTime, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz").withZone(ZoneId.of("Europe/Berlin"));

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        OffsetDateTime odt = OffsetDateTime.parse(json.getAsString()); // Accepts ISO format by default
        return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public String convert(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static Converter.Factory getConverterFactory() {
        return new Converter.Factory() {
            @Nullable
            @Override
            public Converter<LocalDateTime, String> stringConverter(@NonNull Type type, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
                if (type == LocalDateTime.class)
                    return new LocalDateTimeAdapter();
                return null;
            }
        };
    }
}