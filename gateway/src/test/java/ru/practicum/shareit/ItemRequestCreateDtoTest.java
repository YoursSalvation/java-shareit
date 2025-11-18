package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.ItemRequestCreateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestCreateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<ItemRequestCreateDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("need a saw");
        JsonContent<ItemRequestCreateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("need a saw");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"description\":\"looking for a saw\"}";
        ItemRequestCreateDto result = json.parse(content).getObject();
        assertThat(result.getDescription()).isEqualTo("looking for a saw");
    }

    @Test
    void testValidDto() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("need airpods");
        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void descriptionValidation() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(dto);
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