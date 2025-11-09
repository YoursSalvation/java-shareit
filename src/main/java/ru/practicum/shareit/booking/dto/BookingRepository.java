package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;

public interface BookingRepository {

    public Booking getById(Long bookingId);

    public void checkExistenceById(Long bookingId);

    Booking create(Booking booking);

}