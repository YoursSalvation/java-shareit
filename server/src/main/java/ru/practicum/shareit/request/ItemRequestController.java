package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validation.UserIdHeader;

import java.util.Collection;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto create(
            @UserIdHeader Long userId,
            @RequestBody ItemRequestCreateDto itemRequestCreateDto
    ) {
        return itemRequestService.create(userId, itemRequestCreateDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(
            @UserIdHeader Long userId,
            @PathVariable Long requestId
    ) {
        return itemRequestService.getRequestById(requestId);
    }

    @GetMapping
    public Collection<ItemRequestResponseDto> getMysRequests(
            @UserIdHeader Long userId
    ) {
        return itemRequestService.getMyRequests(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestResponseSimpleViewDto> getOthersRequests(
            @UserIdHeader Long userId
    ) {
        return itemRequestService.getOthersRequests(userId);
    }

}