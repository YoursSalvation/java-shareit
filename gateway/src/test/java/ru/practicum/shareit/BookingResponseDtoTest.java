package ru.practicum.shareit;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingResponseDto;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserResponseDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingResponseDtoTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

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

        UserResponseDto booker = new UserResponseDto();
        booker.setId(5L);
        booker.setName("gwen stacy");
        booker.setEmail("gwen.stacy@example.com");

        ItemResponseDto item = new ItemResponseDto();
        item.setId(10L);
        item.setName("perfect saw");
        item.setDescription("real saw");
        item.setAvailable(true);
        item.setRequestId(15L);

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(20L);
        dto.setBooker(booker);
        dto.setItem(item);
        dto.setStart(start);
        dto.setEnd(end);
        dto.setStatus(BookingStatus.APPROVED);

        JsonContent<BookingResponseDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(dto.getBooker().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo(dto.getBooker().getName());
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo(dto.getBooker().getEmail());
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(dto.getItem().getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo(dto.getItem().getName());
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo(dto.getItem().getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(dto.getItem().getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId").isEqualTo(dto.getItem().getRequestId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.atZoneSameInstant(zoneId).format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.atZoneSameInstant(zoneId).format(formatter));
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(dto.getStatus().toString());
    }

    @Test
    void testDeserialize() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(3);
        OffsetDateTime end = OffsetDateTime.now().plusDays(5);
        String formattedStart = start.atZoneSameInstant(zoneId).format(formatter);
        String formattedEnd = end.atZoneSameInstant(zoneId).format(formatter);

        String content = "{\"id\":30,\"booker\":{\"id\":8,\"name\":\"peter parker\",\"email\":\"peter.parker@example.com\"}," +
                "\"item\":{\"id\":14,\"name\":\"real saw\",\"description\":\"real saw\",\"available\":true,\"requestId\":null}," +
                "\"start\":\"" + formattedStart + "\",\"end\":\"" + formattedEnd + "\",\"status\":\"REJECTED\"}";

        BookingResponseDto result = json.parse(content).getObject();
        assertThat(result.getId()).isEqualTo(30L);
        assertThat(result.getBooker().getId()).isEqualTo(8L);
        assertThat(result.getBooker().getName()).isEqualTo("peter parker");
        assertThat(result.getBooker().getEmail()).isEqualTo("peter.parker@example.com");
        assertThat(result.getItem().getId()).isEqualTo(14L);
        assertThat(result.getItem().getName()).isEqualTo("real saw");
        assertThat(result.getItem().getDescription()).isEqualTo("real saw");
        assertThat(result.getItem().getAvailable()).isTrue();
        assertThat(result.getItem().getRequestId()).isNull();
        assertThat(result.getStart()).isEqualTo(LocalDateTime.parse(formattedStart).atZone(zoneId).toOffsetDateTime());
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.parse(formattedEnd).atZone(zoneId).toOffsetDateTime());
        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testFromBookingEntity() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(3);
        OffsetDateTime end = OffsetDateTime.now().plusDays(5);

        User booker = new User();
        booker.setId(9L);
        booker.setName("mary jane");
        booker.setEmail("mary.jane@example.com");

        User owner = new User();
        owner.setId(11L);
        owner.setName("jane ostin");
        owner.setEmail("jane.ostin@example.com");

        Item item = new Item();
        item.setId(16L);
        item.setName("hammer");
        item.setDescription("greatest hammer");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(35L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.CANCELED);

        BookingResponseDto result = BookingResponseDto.from(booking);
        assertThat(result.getId()).isEqualTo(booking.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getBooker().getName()).isEqualTo(booker.getName());
        assertThat(result.getBooker().getEmail()).isEqualTo(booker.getEmail());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getItem().getName()).isEqualTo(item.getName());
        assertThat(result.getItem().getDescription()).isEqualTo(item.getDescription());
        assertThat(result.getItem().getAvailable()).isEqualTo(item.getAvailable());
        assertThat(result.getStart()).isEqualTo(booking.getStart());
        assertThat(result.getEnd()).isEqualTo(booking.getEnd());
        assertThat(result.getStatus()).isEqualTo(booking.getStatus());
    }

    @Test
    void testFromNullBooking() {
        assertThat(BookingResponseDto.from(null)).isNull();
    }

    @Test
    void testAllBookingStatuses() throws Exception {
        OffsetDateTime start = OffsetDateTime.now().plusDays(3);
        OffsetDateTime end = OffsetDateTime.now().plusDays(5);

        for (BookingStatus status : BookingStatus.values()) {
            BookingResponseDto dto = new BookingResponseDto();
            dto.setId(1L);
            dto.setStart(start);
            dto.setEnd(end);
            dto.setStatus(status);
            JsonContent<BookingResponseDto> result = json.write(dto);
            assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(status.name());
        }
    }
}