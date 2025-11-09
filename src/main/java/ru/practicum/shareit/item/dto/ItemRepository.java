package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Item;

import java.util.Collection;

public interface ItemRepository {

    public Item create(Item item);

    public Item update(Item item);

    public Item getById(Long itemId);

    public Collection<Item> findByUserId(Long userId);

    public Collection<Item> findByText(String text);

    public void checkExistenceById(Long itemId);

    public void checkOwnerById(Long itemId, Long userId);

    public void deleteById(Long itemId);
}