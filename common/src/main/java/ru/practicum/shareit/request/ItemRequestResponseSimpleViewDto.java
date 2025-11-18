package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.practicum.shareit.serializer.OffsetDateTimeDeserializer;
import ru.practicum.shareit.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;

@Data
public class ItemRequestResponseSimpleViewDto {

    private Long id;
    private String description;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime created;

    public static ItemRequestResponseSimpleViewDto from(ItemRequest request) {
        if (request == null) return null;
        ItemRequestResponseSimpleViewDto dto = new ItemRequestResponseSimpleViewDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        return dto;
    }

}