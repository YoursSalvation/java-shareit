package ru.practicum.shareit.booking;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Booking {

    private Long id;
    private Long itemId;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private Long bookerId;
    private BookingStatus status;

}