package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponseDto> getList() {
        return userRepository.findAll().stream()
                .filter(Objects::nonNull)
                .map(UserResponseDto::from)
                .toList();
    }

    @Override
    public UserResponseDto getById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        return UserResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = false)
    public UserResponseDto create(UserCreateDto userCreateDto) {
        if (userRepository.existsByEmail(userCreateDto.getEmail())) throw new ConflictException("Duplicate User Email");
        User newUser = new User();
        newUser.setName(userCreateDto.getName());
        newUser.setEmail(userCreateDto.getEmail());
        User createdUser = userRepository.save(newUser);
        return UserResponseDto.from(createdUser);
    }

    @Override
    @Transactional(readOnly = false)
    public UserResponseDto update(UserUpdateDto userUpdateDto, Long userId) {
        User existingUser = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        if (userUpdateDto.getName() != null) {
            existingUser.setName(userUpdateDto.getName());
        }
        if (userUpdateDto.getEmail() != null) {
            boolean emailConflict = userRepository.existsByEmailButNotTheSame(userUpdateDto.getEmail().toLowerCase(),
                    userId);
            if (emailConflict) throw new ConflictException("Duplicate User Email");
            existingUser.setEmail(userUpdateDto.getEmail());
        }
        return UserResponseDto.from(existingUser);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteById(Long userId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("User " + userId + " not found");
        userRepository.deleteById(userId);
    }

}