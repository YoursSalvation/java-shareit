package ru.practicum.shareit.item.api;

import lombok.Data;
import ru.practicum.shareit.item.Item;

@Data
public class ItemResponseDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;

    public static ItemResponseDto from(Item item) {
        if (item == null) return null;
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        return dto;
    }
}