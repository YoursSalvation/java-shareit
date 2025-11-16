package ru.practicum.shareit.item.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.validation.UserIdHeader;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    // CREATE + UPDATE + DELETE OPS

    @PostMapping
    public ItemResponseDto create(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto
    ) {
        return itemService.create(userId, itemCreateDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId,
            @Valid @RequestBody ItemUpdateDto itemUpdateDto
    ) {
        return itemService.update(userId, itemId, itemUpdateDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId
    ) {
        itemService.deleteById(userId, itemId);
    }

    // GET ITEM OPS

    @GetMapping("/{itemId}")
    public ItemResponseExtendedViewDto getById(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId
    ) {
        return itemService.getById(userId, itemId);
    }

    // GET COLLECTION OPS

    @GetMapping
    public Collection<ItemResponseExtendedViewDto> getOwnersItems(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId
    ) {
        return itemService.findByOwnerId(userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponseDto> findByText(
            @RequestParam(required = false) String text
    ) {
        if (text == null || text.isBlank()) return List.of();
        return itemService.findByText(text);
    }

    // COMMENTS OPS

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId,
            @Valid @RequestBody CommentCreateDto commentCreateDto
    ) {
        return itemService.addComment(userId, itemId, commentCreateDto);
    }
}