package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemRepository;
import ru.practicum.shareit.user.dto.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ItemServiceImplement implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item create(Long userId, Item item) {
        userRepository.checkExistenceById(userId);
        item.setOwnerId(userId);
        return itemRepository.create(item);
    }

    @Override
    public Item update(Long userId, Long itemId, Item item) {
        item.setId(itemId);
        userRepository.checkExistenceById(userId);
        itemRepository.checkOwnerById(userId, itemId);
        item.setOwnerId(userId);
        return itemRepository.update(item);
    }

    @Override
    public Item getById(Long itemId) {
        return itemRepository.getById(itemId);
    }

    @Override
    public Collection<Item> findByUserId(Long userId) {
        userRepository.checkExistenceById(userId);
        return itemRepository.findByUserId(userId);
    }

    @Override
    public Collection<Item> findByText(String text) {
        return itemRepository.findByText(text);
    }

    @Override
    public void deleteById(Long userId, Long itemId) {
        userRepository.checkExistenceById(userId);
        itemRepository.checkOwnerById(userId, itemId);
        itemRepository.deleteById(itemId);
    }

}