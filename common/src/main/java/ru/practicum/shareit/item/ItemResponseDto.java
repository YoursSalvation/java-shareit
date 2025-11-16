package ru.practicum.shareit.item;

import lombok.Data;

@Data
public class ItemResponseDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;

    public static ItemResponseDto from(Item item) {
        if (item == null) return null;
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        if (item.getItemRequest() != null) dto.setRequestId(item.getItemRequest().getId());
        return dto;
    }
}