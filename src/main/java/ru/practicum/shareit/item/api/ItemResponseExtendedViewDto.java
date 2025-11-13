package ru.practicum.shareit.item.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.serializer.OffsetDateTimeDeserializer;
import ru.practicum.shareit.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ItemResponseExtendedViewDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;

    private List<CommentResponseDto> comments;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime lastBooking;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime nextBooking;

    public static ItemResponseExtendedViewDto from(Item item) {
        if (item == null) return null;
        ItemResponseExtendedViewDto dto = new ItemResponseExtendedViewDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setComments(
                item.getComments().stream()
                        .map(CommentResponseDto::from)
                        .toList()
        );
        return dto;
    }
}