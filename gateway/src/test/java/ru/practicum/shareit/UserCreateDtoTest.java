package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.UserCreateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserCreateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<UserCreateDto> json;

    @Test
    void testSerialize() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setName("alex alexov");
        dto.setEmail("alex.alexov@yandex.ru");

        JsonContent<UserCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("alex alexov");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("alex.alexov@yandex.ru");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"peter parker\",\"email\":\"peter.parker@yandex.ru\"}";

        UserCreateDto result = json.parse(content).getObject();

        assertThat(result.getName()).isEqualTo("peter parker");
        assertThat(result.getEmail()).isEqualTo("peter.parker@yandex.ru");
    }

    @Test
    void testValidDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setName("gwen stacy");
        dto.setEmail("gwen.stacy@yandex.ru");

        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nameValidation() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("test@yandex.ru");
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setName(" ");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setName("");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setName("a".repeat(101));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void emailValidation() {
        UserCreateDto dto = new UserCreateDto();
        dto.setName("alex young");
        dto.setEmail("invalid-email");
        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setEmail(null);
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setEmail(" ");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(3);

        dto.setEmail("");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setEmail("a".repeat(50) + "@" + "a".repeat(50) + ".ru");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }
}
