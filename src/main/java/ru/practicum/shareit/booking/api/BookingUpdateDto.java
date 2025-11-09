package ru.practicum.shareit.booking.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.serializer.OffsetDateTimeDeserializer;
import ru.practicum.shareit.serializer.OffsetDateTimeSerializer;
import ru.practicum.shareit.validation.AtLeastOneNotNull;

import java.time.OffsetDateTime;

@Data
@AtLeastOneNotNull(fields = {"start", "end", "status"}, message = "Booking update DTO has only null fields")
public class BookingUpdateDto {

    @FutureOrPresent(message = "Field 'start' shouldn't be in past")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime start;

    @FutureOrPresent(message = "Field 'end' shouldn't be in past")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime end;

    private BookingStatus status;

    public Booking toEntity() {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);
        return booking;
    }

}