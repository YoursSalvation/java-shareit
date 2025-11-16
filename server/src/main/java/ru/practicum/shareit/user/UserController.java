package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponseDto post(
            @RequestBody UserCreateDto userCreateDto
    ) {
        return userService.create(userCreateDto);
    }

    @PatchMapping("/{id}")
    public UserResponseDto patch(
            @RequestBody UserUpdateDto userUpdateDto,
            @PathVariable Long id
    ) {
        return userService.update(userUpdateDto, id);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        userService.deleteById(id);
    }

    @GetMapping("/{id}")
    public UserResponseDto getById(
            @PathVariable Long id
    ) {
        return userService.getById(id);
    }

    @GetMapping
    public Collection<UserResponseDto> get() {
        return userService.getList();
    }

}