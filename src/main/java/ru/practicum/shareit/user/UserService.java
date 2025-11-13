package ru.practicum.shareit.user;

import ru.practicum.shareit.user.api.UserCreateDto;
import ru.practicum.shareit.user.api.UserResponseDto;
import ru.practicum.shareit.user.api.UserUpdateDto;

import java.util.List;

public interface UserService {

    List<UserResponseDto> getList();

    UserResponseDto getById(Long userId);

    UserResponseDto create(UserCreateDto user);

    UserResponseDto update(UserUpdateDto user, Long userId);

    void deleteById(Long userId);

}