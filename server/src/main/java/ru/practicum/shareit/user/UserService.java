package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    List<UserResponseDto> getList();

    UserResponseDto getById(Long userId);

    UserResponseDto create(UserCreateDto user);

    UserResponseDto update(UserUpdateDto user, Long userId);

    void deleteById(Long userId);

}