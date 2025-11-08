package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {

    public Item create(Long userId, Item item);

    public Item update(Long userId, Long itemId, Item item);

    public Item getById(Long itemId);

    public Collection<Item> findByUserId(Long userId);

    public Collection<Item> findByText(String text);

    public void deleteById(Long userId, Long itemId);

}