package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.validation.AtLeastOneNotNull;
import ru.practicum.shareit.validation.NotBlankButNullAllowed;

@Data
@AtLeastOneNotNull(fields = {"name", "email"}, message = "User object has only null fields")
public class UserUpdateDto {

    @NotBlankButNullAllowed(message = "Field 'name' shouldn't be blank")
    @Size(min = 1, max = 100, message = "Field 'name' should be from 1 to 100 characters")
    private String name;

    @NotBlankButNullAllowed(message = "Field 'email' shouldn't be blank")
    @Email(message = "Field 'email' should match email mask")
    @Size(min = 3, max = 100, message = "Field 'email' should be from 3 to 100 characters")
    private String email;

}