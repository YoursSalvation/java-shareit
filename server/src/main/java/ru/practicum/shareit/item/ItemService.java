package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {

    ItemResponseDto create(Long userId, ItemCreateDto itemCreateDto);

    ItemResponseDto update(Long userId, Long itemId, ItemUpdateDto itemUpdateDto);

    ItemResponseExtendedViewDto getById(Long userId, Long itemId);

    Collection<ItemResponseExtendedViewDto> findByOwnerId(Long userId);

    Collection<ItemResponseDto> findByText(String text);

    void deleteById(Long userId, Long itemId);

    CommentResponseDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto);

}