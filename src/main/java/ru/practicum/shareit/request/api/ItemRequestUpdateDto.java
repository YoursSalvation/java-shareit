package ru.practicum.shareit.request.api;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

@Data
public class ItemRequestUpdateDto {

    private String description;

    public ItemRequest toEntity() {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        return request;
    }

}