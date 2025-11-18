package ru.practicum.shareit;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemResponseDtoForItemRequests;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseDtoTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

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
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);

        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(1L);
        dto.setDescription("need wooden table");
        dto.setCreated(created);

        ItemResponseDtoForItemRequests item1 = new ItemResponseDtoForItemRequests();
        item1.setId(10L);
        item1.setName("wooden table");
        item1.setOwnerId(5L);

        ItemResponseDtoForItemRequests item2 = new ItemResponseDtoForItemRequests();
        item2.setId(11L);
        item2.setName("wooden chair");
        item2.setOwnerId(6L);

        dto.setItems(List.of(item1, item2));

        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.atZoneSameInstant(zoneId).format(formatter));
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(2);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(item1.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo(item1.getName());
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(item1.getOwnerId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.items[1].id").isEqualTo(item2.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.items[1].name").isEqualTo(item2.getName());
        assertThat(result).extractingJsonPathNumberValue("$.items[1].ownerId").isEqualTo(item2.getOwnerId().intValue());
    }

    @Test
    void testSerializeWithoutItems() throws Exception {
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);

        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(2L);
        dto.setDescription("need real saw");
        dto.setCreated(created);

        JsonContent<ItemRequestResponseDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.atZoneSameInstant(zoneId).format(formatter));
        assertThat(result).extractingJsonPathArrayValue("$.items").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);
        String formattedCreated = created.atZoneSameInstant(zoneId).format(formatter);

        String content = "{\"id\":3,\"description\":\"need kitchen knife\",\"created\":\""
                + formattedCreated + "\",\"items\":[{\"id\":20,\"name\":\"knife\",\"ownerId\":7}]}";

        ItemRequestResponseDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getDescription()).isEqualTo("need kitchen knife");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.parse(formattedCreated).atZone(zoneId).toOffsetDateTime());
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getId()).isEqualTo(20L);
        assertThat(result.getItems().getFirst().getName()).isEqualTo("knife");
        assertThat(result.getItems().getFirst().getOwnerId()).isEqualTo(7L);
    }

    @Test
    void testDeserializeWithoutItems() throws Exception {
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);
        String formattedCreated = created.atZoneSameInstant(zoneId).format(formatter);
        String content = "{\"id\":4,\"description\":\"need keyboard\",\"created\":\"" + formattedCreated + "\"}";

        ItemRequestResponseDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getDescription()).isEqualTo("need keyboard");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.parse(formattedCreated).atZone(zoneId).toOffsetDateTime());
        assertThat(result.getItems()).isNull();
    }

    @Test
    void testFromItemRequestEntity() {
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);

        User owner = new User();
        owner.setId(8L);
        owner.setName("peter parker");
        owner.setEmail("peter.parker@example.com");

        Item item1 = new Item();
        item1.setId(30L);
        item1.setName("real saw");
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(31L);
        item2.setName("knife");
        item2.setOwner(owner);

        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("need saw");
        request.setCreated(created);
        request.setItems(List.of(item1, item2));

        ItemRequestResponseDto result = ItemRequestResponseDto.from(request);

        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo(request.getDescription());
        assertThat(result.getCreated()).isEqualTo(request.getCreated());
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getId()).isEqualTo(item1.getId());
        assertThat(result.getItems().get(0).getName()).isEqualTo(item1.getName());
        assertThat(result.getItems().get(0).getOwnerId()).isEqualTo(owner.getId());
        assertThat(result.getItems().get(1).getId()).isEqualTo(item2.getId());
        assertThat(result.getItems().get(1).getName()).isEqualTo(item2.getName());
        assertThat(result.getItems().get(1).getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void testFromItemRequestEntityWithoutItems() {
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);

        ItemRequest request = new ItemRequest();
        request.setId(6L);
        request.setDescription("need mouse");
        request.setCreated(created);

        ItemRequestResponseDto result = ItemRequestResponseDto.from(request);

        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo(request.getDescription());
        assertThat(result.getCreated()).isEqualTo(request.getCreated());
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void testFromNullItemRequest() {
        assertThat(ItemRequestResponseDto.from(null)).isNull();
    }
}