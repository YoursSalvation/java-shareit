package ru.practicum.shareit.user.api;

import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;

    public static UserResponseDto from(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

}