package ru.practicum.shareit.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingApiState;
import ru.practicum.shareit.booking.BookingCreateDto;
import ru.practicum.shareit.client.HttpClientService;
import ru.practicum.shareit.validation.UserIdHeader;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final HttpClientService httpClientService;

    @PostMapping
    public ResponseEntity<Object> post(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @Valid @RequestBody BookingCreateDto bookingCreateDto
    ) {
        return httpClientService.post("/bookings", userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> patch(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Booking Id not valid") Long bookingId,
            @RequestParam(required = true) @Pattern(regexp = "(?i)true|false") String approved
    ) {
        return httpClientService.patch("/bookings/" + bookingId + "?approved=" + approved, userId, null);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Booking Id not valid") Long bookingId
    ) {
        return httpClientService.get("/bookings/" + bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookerBookings(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingApiState state
    ) {
        return httpClientService.get("/bookings?state=" + state.toString(), userId);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingApiState state
    ) {
        return httpClientService.get("/bookings/owner?state=" + state.toString(), userId);
    }

}