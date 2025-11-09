package ru.practicum.shareit.item.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.item.Item;

@Data
public class ItemCreateDto {

    @NotBlank(message = "Field 'name' shouldn't be blank")
    @Size(min = 1, max = 100, message = "Field 'name' should be from 1 to 100 characters")
    private String name;

    @NotBlank(message = "Field 'description' shouldn't be blank")
    @Size(min = 1, max = 255, message = "Field 'description' should be from 1 to 255 characters")
    private String description;

    @NotNull(message = "Field 'available' shouldn't be null")
    private Boolean available;

    private Long ownerId;
    private Long requestId;

    public Item toEntity() {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwnerId(ownerId);
        item.setRequestId(requestId);
        return item;
    }

}