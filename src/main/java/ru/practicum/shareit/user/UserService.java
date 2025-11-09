package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    public List<User> getList();

    public User getById(Long userId);

    public User create(User user);

    public User update(User user, Long userId);

    public void deleteById(Long userId);

}