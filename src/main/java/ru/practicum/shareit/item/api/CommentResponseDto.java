package ru.practicum.shareit.item.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.serializer.OffsetDateTimeDeserializer;
import ru.practicum.shareit.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;

@Data
public class CommentResponseDto {

    private Long id;
    private String text;
    private String authorName;

    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime created;

    public static CommentResponseDto from(Comment comment) {
        if (comment == null) return null;
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}