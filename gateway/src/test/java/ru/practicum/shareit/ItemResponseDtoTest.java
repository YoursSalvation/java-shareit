package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemResponseDto;
import ru.practicum.shareit.request.ItemRequest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemResponseDtoTest {

    @Autowired
    private JacksonTester<ItemResponseDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(1L);
        dto.setName("item");
        dto.setDescription("lorem ipsum");
        dto.setAvailable(true);
        dto.setRequestId(3L);
        JsonContent<ItemResponseDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(dto.getRequestId().intValue());
    }

    @Test
    void testSerializeWithoutRequestId() throws Exception {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(2L);
        dto.setName("laptop");
        dto.setDescription("great gaming laptop");
        dto.setAvailable(false);
        JsonContent<ItemResponseDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
        assertThat(result).extractingJsonPathValue("$.requestId").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":4,\"name\":\"smartphone\",\"description\":\"new smartphone\",\"available\":true,\"requestId\":7}";
        ItemResponseDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getName()).isEqualTo("smartphone");
        assertThat(result.getDescription()).isEqualTo("new smartphone");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isEqualTo(7L);
    }

    @Test
    void testDeserializeWithoutRequestId() throws Exception {
        String content = "{\"id\":5,\"name\":\"fridge\",\"description\":\"cold fridge\",\"available\":false}";
        ItemResponseDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("fridge");
        assertThat(result.getDescription()).isEqualTo("cold fridge");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void testFromItemEntity() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(8L);

        Item item = new Item();
        item.setId(6L);
        item.setName("table");
        item.setDescription("wooden table");
        item.setAvailable(true);
        item.setItemRequest(itemRequest);

        ItemResponseDto result = ItemResponseDto.from(item);

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getDescription()).isEqualTo(item.getDescription());
        assertThat(result.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(result.getRequestId()).isEqualTo(item.getItemRequest().getId());
    }

    @Test
    void testFromItemEntityWithoutRequest() {
        Item item = new Item();
        item.setId(7L);
        item.setName("chair");
        item.setDescription("wooden chair");
        item.setAvailable(false);

        ItemResponseDto result = ItemResponseDto.from(item);

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getDescription()).isEqualTo(item.getDescription());
        assertThat(result.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void testFromNullItem() {
        assertThat(ItemResponseDto.from(null)).isNull();
    }

}