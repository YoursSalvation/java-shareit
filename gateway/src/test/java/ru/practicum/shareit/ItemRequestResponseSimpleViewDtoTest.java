package ru.practicum.shareit;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestResponseSimpleViewDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestResponseSimpleViewDtoTest {

    @Autowired
    private JacksonTester<ItemRequestResponseSimpleViewDto> json;

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

        ItemRequestResponseSimpleViewDto dto = new ItemRequestResponseSimpleViewDto();
        dto.setId(1L);
        dto.setDescription("need saw");
        dto.setCreated(created);

        JsonContent<ItemRequestResponseSimpleViewDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo(created.atZoneSameInstant(zoneId).format(formatter));
    }

    @Test
    void testDeserialize() throws Exception {
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);
        String formattedCreated = created.atZoneSameInstant(zoneId).format(formatter);
        String content = "{\"id\":2,\"description\":\"need real saw\",\"created\":\"" + formattedCreated + "\"}";

        ItemRequestResponseSimpleViewDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getDescription()).isEqualTo("need real saw");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.parse(formattedCreated).atZone(zoneId).toOffsetDateTime());
    }

    @Test
    void testFromItemRequestEntity() {
        OffsetDateTime created = OffsetDateTime.now().plusDays(3);

        ItemRequest request = new ItemRequest();
        request.setId(3L);
        request.setDescription("need kitchen knife");
        request.setCreated(created);

        ItemRequestResponseSimpleViewDto result = ItemRequestResponseSimpleViewDto.from(request);
        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo(request.getDescription());
        assertThat(result.getCreated()).isEqualTo(request.getCreated());
    }

    @Test
    void testFromNullItemRequest() {
        assertThat(ItemRequestResponseSimpleViewDto.from(null)).isNull();
    }
}