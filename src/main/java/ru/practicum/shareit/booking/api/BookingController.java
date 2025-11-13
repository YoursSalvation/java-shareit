package ru.practicum.shareit.booking.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.validation.UserIdHeader;

import java.util.Collection;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Booking Id not valid") Long bookingId
    ) {
        return bookingService.getById(userId, bookingId);
    }

    @PostMapping
    public BookingResponseDto post(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @Valid @RequestBody BookingCreateDto bookingCreateDto
    ) {
        return bookingService.create(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto patch(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Booking Id not valid") Long bookingId,
            @RequestParam(required = true) @Pattern(regexp = "(?i)true|false") String approved
    ) {
        boolean isApproved = Boolean.parseBoolean(approved);
        return bookingService.approveReject(userId, bookingId, isApproved);
    }

    @GetMapping
    public Collection<BookingResponseDto> getBookerBookings(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingApiState state
    ) {
        return bookingService.getBookerBookings(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getOwnerBookings(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingApiState state
    ) {
        return bookingService.getOwnerBookings(userId, state);
    }


}