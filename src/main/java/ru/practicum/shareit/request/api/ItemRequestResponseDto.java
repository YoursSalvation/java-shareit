package ru.practicum.shareit.request.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.serializer.OffsetDateTimeDeserializer;
import ru.practicum.shareit.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;

@Data
public class ItemRequestResponseDto {

    private Long id;
    private String description;
    private Long requestorId;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime created;

    public static ItemRequestResponseDto from(ItemRequest request) {
        if (request == null) return null;
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        if (request.getRequestor() != null) dto.setRequestorId(request.getRequestor().getId());
        dto.setCreated(request.getCreated());
        return dto;
    }

}