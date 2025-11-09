package ru.practicum.shareit.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OffsetDateTimeSerializer extends StdSerializer<OffsetDateTime> {

    private DateTimeFormatter formatter;

    protected OffsetDateTimeSerializer() {
        super(OffsetDateTime.class);
    }

    @Value("${shareit.api.datetime.format}")
    public void setFormatter(String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Override
    public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(offsetDateTime.format(formatter));
    }

}