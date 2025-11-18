package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.ItemUpdateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemUpdateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<ItemUpdateDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("saw");
        dto.setDescription("real saw");
        dto.setAvailable(true);
        JsonContent<ItemUpdateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
    }

    @Test
    void testSerializeWithNullValues() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("saw");
        dto.setDescription(null);
        dto.setAvailable(null);
        JsonContent<ItemUpdateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("saw");
        assertThat(result).extractingJsonPathValue("$.description").isNull();
        assertThat(result).extractingJsonPathValue("$.available").isNull();

        dto = new ItemUpdateDto();
        dto.setName(null);
        dto.setDescription("item");
        dto.setAvailable(null);
        result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.name").isNull();
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("item");
        assertThat(result).extractingJsonPathValue("$.available").isNull();

        dto = new ItemUpdateDto();
        dto.setName(null);
        dto.setDescription(null);
        dto.setAvailable(false);
        result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.name").isNull();
        assertThat(result).extractingJsonPathValue("$.description").isNull();
        assertThat(result).extractingJsonPathBooleanValue("$.available").isFalse();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"fridge\",\"description\":\"cold fridge\",\"available\":true}";
        ItemUpdateDto result = json.parse(content).getObject();
        assertThat(result.getName()).isEqualTo("fridge");
        assertThat(result.getDescription()).isEqualTo("cold fridge");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void testDeserializeWithNullValues() throws Exception {
        String content = "{\"name\":\"saw\"}";
        ItemUpdateDto result = json.parse(content).getObject();
        assertThat(result.getName()).isEqualTo("saw");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getAvailable()).isNull();

        content = "{\"description\":\"real saw\"}";
        result = json.parse(content).getObject();
        assertThat(result.getName()).isNull();
        assertThat(result.getDescription()).isEqualTo("real saw");
        assertThat(result.getAvailable()).isNull();

        content = "{\"available\":false}";
        result = json.parse(content).getObject();
        assertThat(result.getName()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void testValidDtoWithAllFields() {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("saw");
        dto.setDescription("real good saw");
        dto.setAvailable(true);
        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testAtLeastOneNotNullValidation() {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName("knife");
        dto.setDescription(null);
        dto.setAvailable(null);
        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();

        dto.setName(null);
        dto.setDescription("needle knife");
        dto.setAvailable(null);
        violations = validator.validate(dto);
        assertThat(violations).isEmpty();

        dto.setName(null);
        dto.setDescription(null);
        dto.setAvailable(true);
        violations = validator.validate(dto);
        assertThat(violations).isEmpty();

        dto.setName(null);
        dto.setDescription(null);
        dto.setAvailable(null);
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testNameValidation() {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setName(" ");
        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setName("");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setName("a".repeat(101));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setName("valid name");
        violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testDescriptionValidation() {
        ItemUpdateDto dto = new ItemUpdateDto();
        dto.setDescription(" ");
        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setDescription("");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setDescription("a".repeat(256));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setDescription("valid description");
        violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

}