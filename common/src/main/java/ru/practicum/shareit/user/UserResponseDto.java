package ru.practicum.shareit.user;

import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;

    public static UserResponseDto from(User user) {
        if (user == null) return null;
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

}