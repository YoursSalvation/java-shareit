package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemResponseDtoForItemRequests;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemResponseDtoForItemTest {

    @Autowired
    private JacksonTester<ItemResponseDtoForItemRequests> json;

    @Test
    void testSerialize() throws Exception {
        ItemResponseDtoForItemRequests dto = new ItemResponseDtoForItemRequests();
        dto.setId(1L);
        dto.setName("saw");
        dto.setOwnerId(5L);
        JsonContent<ItemResponseDtoForItemRequests> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(dto.getOwnerId().intValue());
    }

    @Test
    void testSerializeWithNullOwnerId() throws Exception {
        ItemResponseDtoForItemRequests dto = new ItemResponseDtoForItemRequests();
        dto.setId(2L);
        dto.setName("bucket");
        dto.setOwnerId(null);
        JsonContent<ItemResponseDtoForItemRequests> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathValue("$.ownerId").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":3,\"name\":\"knife\",\"ownerId\":7}";
        ItemResponseDtoForItemRequests result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("knife");
        assertThat(result.getOwnerId()).isEqualTo(7L);
    }

    @Test
    void testDeserializeWithNullOwnerId() throws Exception {
        String content = "{\"id\":4,\"name\":\"fork\"}";
        ItemResponseDtoForItemRequests result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getName()).isEqualTo("fork");
        assertThat(result.getOwnerId()).isNull();
    }

    @Test
    void testFromItemEntity() {
        User owner = new User();
        owner.setId(10L);
        owner.setName("peter parker");
        owner.setEmail("peter.parker@example.com");

        Item item = new Item();
        item.setId(8L);
        item.setName("knife");
        item.setDescription("kitchen knife");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemResponseDtoForItemRequests result = ItemResponseDtoForItemRequests.from(item);

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo(item.getName());
        assertThat(result.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void testFromNullItem() {
        assertThat(ItemResponseDtoForItemRequests.from(null)).isNull();
    }
}