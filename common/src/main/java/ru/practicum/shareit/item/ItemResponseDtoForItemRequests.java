package ru.practicum.shareit.item;

import lombok.Data;

@Data
public class ItemResponseDtoForItemRequests {

    private Long id;
    private String name;
    private Long ownerId;

    public static ItemResponseDtoForItemRequests from(Item item) {
        if (item == null) return null;
        ItemResponseDtoForItemRequests dto = new ItemResponseDtoForItemRequests();
        dto.setId(item.getId());
        dto.setName(item.getName());
        if (item.getOwner() != null) dto.setOwnerId(item.getOwner().getId());
        return dto;
    }

}