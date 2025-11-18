package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.UserUpdateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserUpdateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<UserUpdateDto> json;

    @Test
    void testSerialize() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("new name");
        dto.setEmail("newname@yandex.ru");
        JsonContent<UserUpdateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("new name");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("newname@yandex.ru");
    }

    @Test
    void testSerializeWithNullValues() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail(null);
        dto.setName("set name");
        JsonContent<UserUpdateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.email").isNull();
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("set name");

        dto.setEmail("set email");
        dto.setName(null);
        result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("set email");
        assertThat(result).extractingJsonPathValue("$.name").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"peter parker\",\"email\":\"peter.parker@yandex.ru\"}";
        UserUpdateDto result = json.parse(content).getObject();
        assertThat(result.getName()).isEqualTo("peter parker");
        assertThat(result.getEmail()).isEqualTo("peter.parker@yandex.ru");
    }

    @Test
    void testDeserializeWithNullValues() throws Exception {
        String content = "{\"name\":\"gwen stacy\",\"email\":null}";
        UserUpdateDto result = json.parse(content).getObject();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getName()).isEqualTo("gwen stacy");

        content = "{\"name\":null,\"email\":\"alex@yandex.ru\"}";
        result = json.parse(content).getObject();
        assertThat(result.getEmail()).isEqualTo("alex@yandex.ru");
        assertThat(result.getName()).isNull();
    }

    @Test
    void testValidDtoWithBothFields() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("benedict cumberbatch");
        dto.setEmail("benedict.cumberbatch@yandex.ru");
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testAtLeastOneNotNullValidation() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("tony stark");
        dto.setEmail(null);
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();

        dto.setName(null);
        dto.setEmail("tony.stark@yandex.ru");
        violations = validator.validate(dto);
        assertThat(violations).isEmpty();

        dto.setName(null);
        dto.setEmail(null);
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testNameValidation() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setName("");
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setName(" ");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setName("a".repeat(101));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testEmailValidation() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("");
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setEmail(" ");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(3);

        dto.setEmail("invalid-email-format");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setEmail("a@");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setEmail("a".repeat(50) + "@" + "a".repeat(50) + ".ru");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }
}