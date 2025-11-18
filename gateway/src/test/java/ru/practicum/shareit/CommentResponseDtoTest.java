package ru.practicum.shareit;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentResponseDtoTest {

    @Autowired
    private JacksonTester<CommentResponseDto> json;

    @Value("${shareit.api.datetime.format}")
    private String dateTimeFormat;
    private DateTimeFormatter formatter;

    @Value("${shareit.api.datetime.timezone}")
    private String timezone;
    private ZoneId zoneId;

    @PostConstruct
    void setup() {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        this.zoneId = ZoneId.of(timezone);
    }


    @Test
    void testSerialize() throws Exception {
        OffsetDateTime time = OffsetDateTime.now().minusDays(1);
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(1L);
        dto.setText("real item");
        dto.setAuthorName("peter parker");
        dto.setCreated(time);
        JsonContent<CommentResponseDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo(dto.getText());
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo(dto.getAuthorName());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(time.atZoneSameInstant(zoneId).format(formatter));
    }

    @Test
    void testDeserialize() throws Exception {
        OffsetDateTime time = OffsetDateTime.now().minusDays(1);
        String formattedTime = time.atZoneSameInstant(zoneId).format(formatter);
        String content = "{\"id\":2,\"text\":\"greatest item\",\"authorName\":\"gwen stacy\",\"created\":\"" + formattedTime + "\"}";
        CommentResponseDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getText()).isEqualTo("greatest item");
        assertThat(result.getAuthorName()).isEqualTo("gwen stacy");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.parse(formattedTime, formatter).atZone(zoneId).toOffsetDateTime());
    }

    @Test
    void testFromCommentEntity() {
        User author = new User();
        author.setId(1L);
        author.setName("mary jane");

        Comment comment = new Comment();
        comment.setId(3L);
        comment.setText("lorem ipsum");
        comment.setAuthor(author);
        comment.setCreated(OffsetDateTime.parse("2024-03-10T09:15:00+03:00"));

        CommentResponseDto result = CommentResponseDto.from(comment);

        assertThat(result.getId()).isEqualTo(comment.getId());
        assertThat(result.getText()).isEqualTo(comment.getText());
        assertThat(result.getAuthorName()).isEqualTo(comment.getAuthor().getName());
        assertThat(result.getCreated()).isEqualTo(comment.getCreated());
    }

    @Test
    void testFromNullComment() {
        assertThat(CommentResponseDto.from(null)).isNull();
    }
}