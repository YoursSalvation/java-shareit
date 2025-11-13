package ru.practicum.shareit.user.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<UserResponseDto> get() {
        return userService.getList();
    }

    @GetMapping("/{id}")
    public UserResponseDto getById(
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        return userService.getById(id);
    }

    @PostMapping
    public UserResponseDto post(
            @Valid @RequestBody UserCreateDto userCreateDto
    ) {
        return userService.create(userCreateDto);
    }

    @PatchMapping("/{id}")
    public UserResponseDto patch(
            @Valid @RequestBody UserUpdateDto userUpdateDto,
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        return userService.update(userUpdateDto, id);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        userService.deleteById(id);
    }

}