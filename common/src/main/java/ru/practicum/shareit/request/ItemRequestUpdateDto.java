package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemRequestUpdateDto {

    @NotBlank(message = "Field 'description' shouldn't be blank")
    @Size(min = 1, max = 255, message = "Field 'description' should be from 1 to 255 characters")
    private String description;

}