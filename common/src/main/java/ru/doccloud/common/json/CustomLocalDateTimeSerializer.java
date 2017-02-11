package ru.doccloud.common.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ru.doccloud.common.DateTimeConstants;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 *
 */
public class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(DateTimeConstants.TIMESTAMP_PATTERN);

    CustomLocalDateTimeSerializer() {
    }

    @Override
    public void serialize(LocalDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider provider)
            throws IOException, JsonGenerationException
    {
        if (dateTime == null) {
            jsonGenerator.writeNull();
        }
        else {
            jsonGenerator.writeString(dateTimeFormat.print(dateTime));
        }
    }
}
