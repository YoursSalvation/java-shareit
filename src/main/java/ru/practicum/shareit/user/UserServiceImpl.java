package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getList() {
        return userRepository.findList();
    }

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User create(User user) {
        return userRepository.create(user);
    }

    @Override
    public User update(User user, Long userId) {
        user.setId(userId);
        return userRepository.update(user);
    }

    @Override
    public void deleteById(Long userId) {
        userRepository.deleteById(userId);
    }

}