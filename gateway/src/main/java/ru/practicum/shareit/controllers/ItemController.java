package ru.practicum.shareit.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.HttpClientService;
import ru.practicum.shareit.item.CommentCreateDto;
import ru.practicum.shareit.item.ItemCreateDto;
import ru.practicum.shareit.item.ItemUpdateDto;
import ru.practicum.shareit.validation.UserIdHeader;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final HttpClientService httpClientService;

    @PostMapping
    public ResponseEntity<Object> create(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto
    ) {
        return httpClientService.post("/items", userId, itemCreateDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId,
            @Valid @RequestBody ItemUpdateDto itemUpdateDto
    ) {
        return httpClientService.patch("/items/" + itemId, userId, itemUpdateDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId
    ) {
        return httpClientService.delete("/items/" + itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId
    ) {
        return httpClientService.get("/items/" + itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnersItems(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId
    ) {
        return httpClientService.get("/items", userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findByText(
            @RequestParam(required = false) String text
    ) {
        if (text == null || text.isBlank()) return new ResponseEntity<>(List.of(), HttpStatus.OK);
        return httpClientService.get("/items/search?text=" + text, null);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId,
            @Valid @RequestBody CommentCreateDto commentCreateDto
    ) {
        return httpClientService.post("/items/" + itemId + "/comment", userId, commentCreateDto);
    }

}