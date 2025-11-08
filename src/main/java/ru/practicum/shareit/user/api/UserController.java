package ru.practicum.shareit.user.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<UserResponseDto> get() {
        return userService.getList().stream()
                .filter(Objects::nonNull)
                .map(UserResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponseDto getById(
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        return UserResponseDto.from(userService.getById(id));
    }

    @PostMapping
    public UserResponseDto post(
            @Valid @RequestBody UserCreateDto userCreateDto
    ) {
        return UserResponseDto.from(userService.create(userCreateDto.toEntity()));
    }

    @PatchMapping("/{id}")
    public UserResponseDto patch(
            @Valid @RequestBody UserUpdateDto userUpdateDto,
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        return UserResponseDto.from(userService.update(userUpdateDto.toEntity(), id));
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        userService.deleteById(id);
    }

}