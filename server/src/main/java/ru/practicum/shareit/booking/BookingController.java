package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validation.UserIdHeader;

import java.util.Collection;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto post(
            @UserIdHeader Long userId,
            @RequestBody BookingCreateDto bookingCreateDto
    ) {
        return bookingService.create(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto patch(
            @UserIdHeader Long userId,
            @PathVariable Long bookingId,
            @RequestParam(required = true) String approved
    ) {
        boolean isApproved = Boolean.parseBoolean(approved);
        return bookingService.approveReject(userId, bookingId, isApproved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(
            @UserIdHeader Long userId,
            @PathVariable Long bookingId
    ) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingResponseDto> getBookerBookings(
            @UserIdHeader Long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingApiState state
    ) {
        return bookingService.getBookerBookings(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getOwnerBookings(
            @UserIdHeader Long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingApiState state
    ) {
        return bookingService.getOwnerBookings(userId, state);
    }

}