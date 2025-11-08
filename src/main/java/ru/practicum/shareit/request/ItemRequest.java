package ru.practicum.shareit.request;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ItemRequest {

    private Long id;
    private String description;
    private Long requestId;
    private OffsetDateTime created;

}