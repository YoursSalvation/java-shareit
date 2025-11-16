package ru.practicum.shareit;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingCreateDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private JacksonTester<BookingCreateDto> json;
    @Value("${shareit.api.datetime.format}")
    private String dateTimeFormat;
    private DateTimeFormatter formatter;

    @Value("${shareit.api.datetime.timezone}")
    private String timezone;
    private ZoneId zoneId;

    @PostConstruct
    void setup() {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        this.zoneId = ZoneId.of(timezone);
    }

    @Test
    void testSerialize() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(3);
        OffsetDateTime end = OffsetDateTime.now().plusDays(5);
        String formattedStart = start.atZoneSameInstant(zoneId).format(formatter);
        String formattedEnd = end.atZoneSameInstant(zoneId).format(formatter);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(15L);
        dto.setStart(start);
        dto.setEnd(end);

        JsonContent<BookingCreateDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(15);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(formattedStart);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(formattedEnd);
    }

    @Test
    void testDeserialize() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(3);
        OffsetDateTime end = OffsetDateTime.now().plusDays(5);
        String formattedStart = start.atZoneSameInstant(zoneId).format(formatter);
        String formattedEnd = end.atZoneSameInstant(zoneId).format(formatter);

        String content = "{\"itemId\":25,\"start\":\"" + formattedStart + "\",\"end\":\"" + formattedEnd + "\"}";

        BookingCreateDto result = json.parse(content).getObject();
        assertThat(result.getItemId()).isEqualTo(25L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.parse(formattedStart).atZone(zoneId).toOffsetDateTime());
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.parse(formattedEnd).atZone(zoneId).toOffsetDateTime());
    }

    @Test
    void testValidDto() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(10L);
        dto.setStart(OffsetDateTime.now().plusDays(1));
        dto.setEnd(OffsetDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void itemIdValidation() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setStart(OffsetDateTime.now().plusDays(1));
        dto.setEnd(OffsetDateTime.now().plusDays(2));
        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setItemId(0L);
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setItemId(-5L);
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void startValidation() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setEnd(OffsetDateTime.now().plusDays(2));
        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setStart(OffsetDateTime.now().minusDays(1));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void endValidation() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(OffsetDateTime.now().plusDays(1));
        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(1);

        dto.setEnd(OffsetDateTime.now().minusDays(1));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(1);
    }

    @Test
    void multipleValidationErrors() {
        BookingCreateDto dto = new BookingCreateDto();
        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(dto);
        assertThat(violations).hasSize(3);

        dto.setItemId(-1L);
        violations = validator.validate(dto);
        assertThat(violations).hasSize(3);

        dto.setStart(OffsetDateTime.now().minusDays(2));
        dto.setEnd(OffsetDateTime.now().minusDays(1));
        violations = validator.validate(dto);
        assertThat(violations).hasSize(3);
    }
}