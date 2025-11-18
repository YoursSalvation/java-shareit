package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.ItemCreateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemCreateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<ItemCreateDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("real saw");
        dto.setDescription("professional saw");
        dto.setAvailable(true);
        dto.setRequestId(5L);
        JsonContent<ItemCreateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(dto.getRequestId().intValue());
    }

    @Test
    void testSerializeWithoutRequestId() throws Exception {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("bicycle");
        dto.setDescription("mountain bike");
        dto.setAvailable(false);
        JsonContent<ItemCreateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
        assertThat(result).extractingJsonPathValue("$.requestId").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"name\":\"laptop\",\"description\":\"gaming laptop\",\"available\":true,\"requestId\":10}";
        ItemCreateDto result = json.parse(content).getObject();
        assertThat(result.getName()).isEqualTo("laptop");
        assertThat(result.getDescription()).isEqualTo("gaming laptop");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isEqualTo(10L);
    }

    @Test
    void testDeserializeWithoutRequestId() throws Exception {
        String content = "{\"name\":\"camera\",\"description\":\"digital camera\",\"available\":false}";
        ItemCreateDto result = json.parse(content).getObject();
        assertThat(result.getName()).isEqualTo("camera");
        assertThat(result.getDescription()).isEqualTo("digital camera");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void testValidDto() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("smartphone");
        dto.setDescription("latest model");
        dto.setAvailable(true);

        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void nameValidation() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setDescription("test description");
        dto.setAvailable(true);
        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(dto);
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
    void descriptionValidation() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("test item");
        dto.setAvailable(true);
        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setDescription(" ");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setDescription("");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setDescription("a".repeat(256)); // 256 characters
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void availableValidation() {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setName("test item");
        dto.setDescription("test description");
        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

}