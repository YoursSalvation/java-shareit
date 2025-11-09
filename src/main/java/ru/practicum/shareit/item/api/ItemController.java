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
import java.util.Objects;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto create(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto
    ) {
        return ItemResponseDto.from(itemService.create(userId, itemCreateDto.toEntity()));
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId,
            @Valid @RequestBody ItemUpdateDto itemUpdateDto
    ) {
        return ItemResponseDto.from(itemService.update(userId, itemId, itemUpdateDto.toEntity()));
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getById(
            @PathVariable @Positive(message = "Item Id not valid") Long itemId
    ) {
        return ItemResponseDto.from(itemService.getById(itemId));
    }

    @GetMapping
    public Collection<ItemResponseDto> findByUserId(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId
    ) {
        return itemService.findByUserId(userId).stream()
                .filter(Objects::nonNull)
                .map(ItemResponseDto::from)
                .toList();
    }

    @GetMapping("/search")
    public Collection<ItemResponseDto> findByText(
            @RequestParam(required = false) String text
    ) {
        if (text == null || text.isBlank()) return List.of();
        return itemService.findByText(text).stream()
                .filter(Objects::nonNull)
                .map(ItemResponseDto::from)
                .toList();
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @UserIdHeader @Positive(message = "User Id not valid") Long userId,
            @PathVariable @Positive(message = "Item Id not valid") Long itemId
    ) {
        itemService.deleteById(userId, itemId);
    }

}