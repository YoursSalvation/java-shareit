package ru.practicum.shareit;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.CommentCreateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentCreateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<CommentCreateDto> json;

    @Test
    void testSerialize() throws Exception {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("real item");
        JsonContent<CommentCreateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("real item");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"text\":\"fast delivery\"}";
        CommentCreateDto result = json.parse(content).getObject();
        assertThat(result.getText()).isEqualTo("fast delivery");
    }

    @Test
    void testValidDto() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("real item");
        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void textValidation() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText(null);
        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setText(" ");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setText("");
        violations = validator.validate(dto);
        assertThat(violations).hasSize(2);

        dto.setText("a".repeat(513));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }
}