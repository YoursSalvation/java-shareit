package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validation.UserIdHeader;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto create(
            @UserIdHeader Long userId,
            @RequestBody ItemCreateDto itemCreateDto
    ) {
        return itemService.create(userId, itemCreateDto);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @UserIdHeader Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto itemUpdateDto
    ) {
        return itemService.update(userId, itemId, itemUpdateDto);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @UserIdHeader Long userId,
            @PathVariable Long itemId
    ) {
        itemService.deleteById(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseExtendedViewDto getById(
            @UserIdHeader Long userId,
            @PathVariable Long itemId
    ) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemResponseExtendedViewDto> getOwnersItems(
            @UserIdHeader Long userId
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

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @UserIdHeader Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentCreateDto commentCreateDto
    ) {
        return itemService.addComment(userId, itemId, commentCreateDto);
    }

}