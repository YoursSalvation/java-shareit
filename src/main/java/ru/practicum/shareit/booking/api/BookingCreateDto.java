package ru.practicum.shareit.booking.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.practicum.shareit.serializer.OffsetDateTimeDeserializer;
import ru.practicum.shareit.serializer.OffsetDateTimeSerializer;
import ru.practicum.shareit.validation.FutureOrPresentWithTolerance;

import java.time.OffsetDateTime;

@Data
public class BookingCreateDto {

    @NotNull(message = "Field 'itemId' shouldn't be null")
    @Positive(message = "Field 'itemId' should be positive")
    private Long itemId;

    @NotNull(message = "Field 'start' shouldn't be null")
    @FutureOrPresentWithTolerance(message = "Field 'start' shouldn't be in past")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime start;

    @NotNull(message = "Field 'end' shouldn't be null")
    @FutureOrPresentWithTolerance(message = "Field 'end' shouldn't be in past")
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime end;
}