package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.practicum.shareit.item.ItemResponseDtoForItemRequests;
import ru.practicum.shareit.serializer.OffsetDateTimeDeserializer;
import ru.practicum.shareit.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Data
public class ItemRequestResponseDto {

    private Long id;
    private String description;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime created;

    private List<ItemResponseDtoForItemRequests> items;

    public static ItemRequestResponseDto from(ItemRequest request) {
        if (request == null) return null;
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        if (request.getItems() != null) {
            dto.setItems(
                    request.getItems().stream()
                            .filter(Objects::nonNull)
                            .map(ItemResponseDtoForItemRequests::from)
                            .toList()
            );
        }
        return dto;
    }

}