package ru.practicum.shareit;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemResponseExtendedViewDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemResponseExtendedViewDtoTest {

    @Autowired
    private JacksonTester<ItemResponseExtendedViewDto> json;

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
        OffsetDateTime last = OffsetDateTime.now().minusDays(10);
        OffsetDateTime next = OffsetDateTime.now().plusDays(10);

        ItemResponseExtendedViewDto dto = new ItemResponseExtendedViewDto();
        dto.setId(1L);
        dto.setName("real saw");
        dto.setDescription("lorem ipsum");
        dto.setAvailable(true);
        dto.setComments(new ArrayList<>());
        dto.setLastBooking(last);
        dto.setNextBooking(next);

        JsonContent<ItemResponseExtendedViewDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
        assertThat(result).extractingJsonPathStringValue("$.lastBooking").isEqualTo(last.atZoneSameInstant(zoneId).format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.nextBooking").isEqualTo(next.atZoneSameInstant(zoneId).format(formatter));
    }

    @Test
    void testSerializeWithNullBookings() throws Exception {
        ItemResponseExtendedViewDto dto = new ItemResponseExtendedViewDto();
        dto.setId(2L);
        dto.setName("hammer");
        dto.setDescription("real hammer");
        dto.setAvailable(false);
        dto.setComments(new ArrayList<>());

        JsonContent<ItemResponseExtendedViewDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        OffsetDateTime last = OffsetDateTime.now().minusDays(10);
        OffsetDateTime next = OffsetDateTime.now().plusDays(10);
        String formattedLast = last.atZoneSameInstant(zoneId).format(formatter);
        String formattedNext = last.atZoneSameInstant(zoneId).format(formatter);

        String content = "{\"id\":3,\"name\":\"fridge\",\"description\":\"cold fridge\",\"available\":true,\"comments\":[],\"lastBooking\":\""
                + formattedLast + "\",\"nextBooking\":\"" + formattedNext + "\"}";

        ItemResponseExtendedViewDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("fridge");
        assertThat(result.getDescription()).isEqualTo("cold fridge");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getLastBooking()).isEqualTo(LocalDateTime.parse(formattedLast, formatter).atZone(zoneId).toOffsetDateTime());
        assertThat(result.getNextBooking()).isEqualTo(LocalDateTime.parse(formattedNext, formatter).atZone(zoneId).toOffsetDateTime());
    }

    @Test
    void testDeserializeWithNullBookings() throws Exception {
        String content = "{\"id\":4,\"name\":\"saw\",\"description\":\"real saw\",\"available\":false,\"comments\":[],\"lastBooking\":null,\"nextBooking\":null}";

        ItemResponseExtendedViewDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getName()).isEqualTo("saw");
        assertThat(result.getDescription()).isEqualTo("real saw");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void testFromItemEntity() {
        User commentAuthor = new User();
        commentAuthor.setId(1L);
        commentAuthor.setName("peter parker");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("lorem ipsum");
        comment.setAuthor(commentAuthor);
        comment.setCreated(OffsetDateTime.parse("2024-01-15T10:30:00+03:00"));

        Item item = new Item();
        item.setId(5L);
        item.setName("items");
        item.setDescription("lorem ipsum");
        item.setAvailable(true);
        item.setComments(List.of(comment));

        ItemResponseExtendedViewDto result = ItemResponseExtendedViewDto.from(item);

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getDescription()).isEqualTo(item.getDescription());
        assertThat(result.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().getFirst().getId()).isEqualTo(comment.getId());
        assertThat(result.getComments().getFirst().getText()).isEqualTo(comment.getText());
        assertThat(result.getComments().getFirst().getAuthorName()).isEqualTo(comment.getAuthor().getName());
        assertThat(result.getComments().getFirst().getCreated()).isEqualTo(comment.getCreated());
    }

    @Test
    void testFromItemEntityWithEmptyComments() {
        Item item = new Item();
        item.setId(6L);
        item.setName("real saw");
        item.setDescription("real saw");
        item.setAvailable(false);
        item.setComments(new ArrayList<>());

        ItemResponseExtendedViewDto result = ItemResponseExtendedViewDto.from(item);

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getDescription()).isEqualTo(item.getDescription());
        assertThat(result.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void testFromNullItem() {
        assertThat(ItemResponseExtendedViewDto.from(null)).isNull();
    }
}