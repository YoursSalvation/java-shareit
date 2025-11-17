package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserResponseDtoTest {

    @Autowired
    private JacksonTester<UserResponseDto> json;

    @Test
    void testSerialize() throws Exception {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(1L);
        dto.setName("peter parker");
        dto.setEmail("peter.parker@yandex.ru");
        JsonContent<UserResponseDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("peter parker");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("peter.parker@yandex.ru");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":2,\"name\":\"gwen stacy\",\"email\":\"gwen.stacy@yandex.ru\"}";
        UserResponseDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("gwen stacy");
        assertThat(result.getEmail()).isEqualTo("gwen.stacy@yandex.ru");
    }

    @Test
    void testFromUserEntity() {
        User user = new User();
        user.setId(3L);
        user.setName("benedict cumberbatch");
        user.setEmail("benedict.cumberbatch@yandex.ru");
        UserResponseDto result = UserResponseDto.from(user);
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void testFromNullUser() {
        assertThat(UserResponseDto.from(null)).isNull();
    }
}