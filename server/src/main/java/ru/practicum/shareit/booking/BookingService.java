package ru.practicum.shareit.booking;

import java.util.Collection;

public interface BookingService {

    BookingResponseDto getById(
            Long userId,
            Long bookingId
    );

    BookingResponseDto create(
            Long userId,
            BookingCreateDto bookingCreateDto
    );

    BookingResponseDto approveReject(
            Long userId,
            Long bookingId,
            boolean isApproved
    );

    Collection<BookingResponseDto> getBookerBookings(
            Long userId,
            BookingApiState state
    );

    Collection<BookingResponseDto> getOwnerBookings(
            Long userId,
            BookingApiState state
    );

}