package ru.practicum.shareit.booking;

public interface BookingService {

    public Booking getById(Long userId, Long bookingId);

    Booking create(Long userId, Booking booking);
}