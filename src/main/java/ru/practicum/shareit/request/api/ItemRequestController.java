package ru.practicum.shareit.request.api;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(
            @PathVariable @Positive(message = "Request Id not valid") Long requestId
    ) {
        return null;
    }

}