package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserRepository {

    public List<User> findList();

    public User findById(Long userId);

    public void checkExistenceById(Long userId);

    public User create(User user);

    public User update(User user);

    public void deleteById(Long userId);

}