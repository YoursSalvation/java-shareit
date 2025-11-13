package ru.practicum.shareit.item.api;

import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.validation.AtLeastOneNotNull;
import ru.practicum.shareit.validation.NotBlankButNullAllowed;

@Data
@AtLeastOneNotNull(fields = {"name", "description", "available"}, message = "Item update DTO has only null fields")
public class ItemUpdateDto {

    @NotBlankButNullAllowed(message = "Field 'name' shouldn't be blank")
    @Size(min = 1, max = 100, message = "Field 'name' should be from 1 to 100 characters")
    private String name;

    @NotBlankButNullAllowed(message = "Field 'description' shouldn't be blank")
    @Size(min = 1, max = 255, message = "Field 'description' should be from 1 to 255 characters")
    private String description;

    private Boolean available;

}