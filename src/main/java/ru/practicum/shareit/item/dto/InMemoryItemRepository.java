package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();

    @Override
    public Item create(Item item) {
        item.setId(nextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        checkExistenceById(item.getId());
        Item existingItem = items.get(item.getId());
        if (item.getName() != null && !Objects.equals(item.getName(), existingItem.getName())) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null && !Objects.equals(item.getDescription(), existingItem.getDescription())) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null && !Objects.equals(item.getAvailable(), existingItem.getAvailable())) {
            existingItem.setAvailable(item.getAvailable());
        }
        return existingItem;
    }

    @Override
    public Item getById(Long itemId) {
        checkExistenceById(itemId);
        return items.get(itemId);
    }

    @Override
    public Collection<Item> findByUserId(Long userId) {
        return items.values().stream()
                .filter(Objects::nonNull)
                .filter(i -> Objects.equals(i.getOwnerId(), userId))
                .toList();
    }

    @Override
    public Collection<Item> findByText(String text) {
        return items.values().stream()
                .filter(Objects::nonNull)
                .filter(Item::getAvailable)
                .filter(i -> containsText(i, text))
                .toList();
    }

    @Override
    public void checkExistenceById(Long itemId) {
        if (itemId == null || !items.containsKey(itemId))
            throw new NotFoundException("Item " + itemId + " not found");
    }

    @Override
    public void checkOwnerById(Long userId, Long itemId) {
        checkExistenceById(itemId);
        Item existingItem = items.get(itemId);
        if (!Objects.equals(existingItem.getOwnerId(), userId))
            throw new ForbiddenException("Owner check failed");
    }

    @Override
    public void deleteById(Long itemId) {
        checkExistenceById(itemId);
        items.remove(itemId);
    }

    private Long nextId() {
        return items.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(0L) + 1L;
    }

    private boolean containsText(Item item, String text) {
        if (item.getName() != null &&
                item.getName().toLowerCase().contains(text.toLowerCase()))
            return true;
        if (item.getDescription() != null &&
                item.getDescription().toLowerCase().contains(text.toLowerCase()))
            return true;
        return false;
    }

}