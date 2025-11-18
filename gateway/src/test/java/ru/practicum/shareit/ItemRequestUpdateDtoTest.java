package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.ItemRequestUpdateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestUpdateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<ItemRequestUpdateDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestUpdateDto dto = new ItemRequestUpdateDto();
        dto.setDescription("updated real saw");
        JsonContent<ItemRequestUpdateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("updated real saw");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"description\":\"mod real saw\"}";
        ItemRequestUpdateDto result = json.parse(content).getObject();
        assertThat(result.getDescription()).isEqualTo("mod real saw");
    }

    @Test
    void testValidDto() {
        ItemRequestUpdateDto dto = new ItemRequestUpdateDto();
        dto.setDescription("lorem ipsum");
        Set<ConstraintViolation<ItemRequestUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void descriptionValidation() {
        ItemRequestUpdateDto dto = new ItemRequestUpdateDto();
        Set<ConstraintViolation<ItemRequestUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setDescription(" ");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setDescription("");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setDescription("a".repeat(256));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }
}