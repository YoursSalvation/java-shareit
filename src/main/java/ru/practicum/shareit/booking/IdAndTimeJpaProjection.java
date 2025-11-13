package ru.practicum.shareit.booking;

import java.time.OffsetDateTime;

public interface IdAndTimeJpaProjection {

    Long getId();

    OffsetDateTime getTime();
}