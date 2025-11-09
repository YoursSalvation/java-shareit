package ru.practicum.shareit.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class OffsetDateTimeDeserializer extends StdDeserializer<OffsetDateTime> {

    private DateTimeFormatter formatter;
    private ZoneId zoneId;

    @Value("${shareit.api.datetime.format}")
    public void setFormatter(String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Value("${shareit.api.datetime.timezone}")
    public void setZoneId(String timezone) {
        this.zoneId = ZoneId.of(timezone);
    }

    public OffsetDateTimeDeserializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String date = jsonParser.getText();
        return LocalDateTime.parse(date, formatter).atZone(zoneId).toOffsetDateTime();
    }

}