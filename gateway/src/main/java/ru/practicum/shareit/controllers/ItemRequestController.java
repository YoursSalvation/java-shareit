package ru.practicum.shareit.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.HttpClientService;
import ru.practicum.shareit.request.ItemRequestCreateDto;
import ru.practicum.shareit.validation.UserIdHeader;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final HttpClientService httpClientService;

    @PostMapping
    public ResponseEntity<Object> create(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto
    ) {
        return httpClientService.post("/requests", userId, itemRequestCreateDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Request Id not valid") Long requestId
    ) {
        return httpClientService.get("/requests/" + requestId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getMysRequests(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId
    ) {
        return httpClientService.get("/requests", userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getOthersRequests(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId
    ) {
        return httpClientService.get("/requests/all", userId);
    }

}