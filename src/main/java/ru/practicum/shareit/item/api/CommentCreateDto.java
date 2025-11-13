package ru.practicum.shareit.item.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDto {

    @NotBlank(message = "Field 'text' shouldn't be blank")
    @Size(min = 1, max = 512, message = "Field 'text' should be from 1 to 512 characters")
    private String text;

}