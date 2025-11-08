package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookingRepository implements BookingRepository {

    private final Map<Long, Booking> bookings = new ConcurrentHashMap<>();

    @Override
    public Booking getById(Long bookingId) {
        checkExistenceById(bookingId);
        return bookings.get(bookingId);
    }

    @Override
    public void checkExistenceById(Long bookingId) {
        if (bookingId == null || !bookings.containsKey(bookingId))
            throw new NotFoundException("Booking " + bookingId + " not found");
    }

    @Override
    public Booking create(Booking booking) {
        booking.setId(nextId());
        bookings.put(booking.getId(), booking);
        return booking;
    }

    private Long nextId() {
        return bookings.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(0L) + 1L;
    }


}