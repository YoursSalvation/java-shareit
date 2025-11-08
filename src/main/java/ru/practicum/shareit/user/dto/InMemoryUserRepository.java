package ru.practicum.shareit.user.dto;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    @Override
    public List<User> findList() {
        return users.values().stream().toList();
    }

    @Override
    public User findById(Long userId) {
        checkExistenceById(userId);
        return users.get(userId);
    }

    @Override
    public void checkExistenceById(Long userId) {
        if (userId == null || !users.containsKey(userId)) throw new NotFoundException("User " + userId + " not found");
    }

    @Override
    public User create(User user) {
        checkDuplicateEmail(user.getEmail());
        user.setId(nextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        checkExistenceById(user.getId());
        User existingUser = users.get(user.getId());
        if (user.getEmail() != null && !Objects.equals(user.getEmail(), existingUser.getEmail())) {
            checkDuplicateEmail(user.getEmail());
            existingUser.setEmail(user.getEmail());
        }
        if (user.getName() != null && !Objects.equals(user.getName(), existingUser.getName())) {
            existingUser.setName(user.getName());
        }
        return existingUser;
    }

    @Override
    public void deleteById(Long userId) {
        checkExistenceById(userId);
        users.remove(userId);
    }

    private Long nextId() {
        return users.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(0L) + 1L;
    }

    private void checkDuplicateEmail(String email) {
        boolean emailExists = users.values().stream()
                .anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
        if (emailExists) throw new ConflictException("Duplicate User Email");
    }

}