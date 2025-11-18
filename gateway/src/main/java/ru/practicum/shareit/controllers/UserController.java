package ru.practicum.shareit.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.HttpClientService;
import ru.practicum.shareit.user.UserCreateDto;
import ru.practicum.shareit.user.UserUpdateDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final HttpClientService httpClientService;

    @GetMapping
    public ResponseEntity<Object> get() {
        return httpClientService.get("/users", null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        return httpClientService.get("/users/" + id, null);
    }

    @PostMapping
    public ResponseEntity<Object> post(
            @Valid @RequestBody UserCreateDto userCreateDto
    ) {
        return httpClientService.post("/users", null, userCreateDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patch(
            @Valid @RequestBody UserUpdateDto userUpdateDto,
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        return httpClientService.patch("/users/" + id, null, userUpdateDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable @Positive(message = "User Id not valid") Long id
    ) {
        return httpClientService.delete("/users/" + id, null);
    }

}