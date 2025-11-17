package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.booking.BookingCreateDto;
import ru.practicum.shareit.booking.BookingResponseDto;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.client.HttpClientService;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.item.ItemResponseDto;
import ru.practicum.shareit.item.ItemUpdateDto;
import ru.practicum.shareit.user.UserResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @MockitoBean
    private HttpClientService httpClientService;

    @Autowired
    private WebApplicationContext context;

    @Value("${shareit.api.auth.userheader}")
    private String userIdHeader;

    @Value("${shareit.api.datetime.format}")
    private String dateTimeFormat;
    private DateTimeFormatter formatter;

    @Value("${shareit.api.datetime.timezone}")
    private String timezone;
    private ZoneId zoneId;

    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(userIdHeader, "1"))
                .build();
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        this.zoneId = ZoneId.of(timezone);
    }

    @Test
    void badRequests() throws Exception {
        mvc.perform(post("/bookings").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(post("/bookings").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
        mvc.perform(post("/bookings").content("{\"king\":\"sauron\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        mvc.perform(patch("/bookings/1"))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(patch("/bookings/1?approved=1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(patch("/bookings/1?approved=yes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));

        mvc.perform(get("/bookings/-1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));

    }

    @Test
    void itemIdValidation() throws Exception {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setStart(OffsetDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(OffsetDateTime.now().plusDays(2));

        mvc.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        bookingCreateDto.setItemId(-1L);
        mvc.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void startValidation() throws Exception {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setEnd(OffsetDateTime.now().plusDays(2));

        mvc.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        bookingCreateDto.setStart(OffsetDateTime.now().minusDays(1));
        mvc.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void endValidation() throws Exception {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(OffsetDateTime.now().plusDays(1));

        mvc.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        bookingCreateDto.setEnd(OffsetDateTime.now().minusDays(1));
        mvc.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void notFoundRequests() throws Exception {
        ErrorResponse notFoundResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND)
                .error("Not Found")
                .message("Booking 1 not found")
                .path("/bookings")
                .build();
        ResponseEntity<Object> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);

        when(httpClientService.get(eq("/bookings/1"), eq(1L))).thenReturn(responseEntity);
        mvc.perform(get("/bookings/1").content(""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        when(httpClientService.patch(eq("/bookings/1?approved=false"), eq(1L), any())).thenReturn(responseEntity);
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("new name");
        mvc.perform(patch("/bookings/1?approved=false").content(mapper.writeValueAsString(itemUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void wellWork() throws Exception {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(OffsetDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(OffsetDateTime.now().plusDays(2));

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setEmail("mail@yandex.ru");
        userResponseDto.setName("alex");

        ItemResponseDto itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("saw");
        itemResponseDto.setDescription("real saw");
        itemResponseDto.setAvailable(true);

        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(1L);
        bookingResponseDto.setBooker(userResponseDto);
        bookingResponseDto.setItem(itemResponseDto);
        bookingResponseDto.setStart(OffsetDateTime.now().plusDays(1));
        bookingResponseDto.setEnd(OffsetDateTime.now().plusDays(2));
        bookingResponseDto.setStatus(BookingStatus.WAITING);

        when(httpClientService.post(eq("/bookings"), eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(bookingResponseDto));

        mvc.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.booker.email", is(bookingResponseDto.getBooker().getEmail())))
                .andExpect(jsonPath("$.booker.name", is(bookingResponseDto.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(bookingResponseDto.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(bookingResponseDto.getItem().getAvailable())))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())));

        when(httpClientService.patch(eq("/bookings/1?approved=true"), eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(bookingResponseDto));

        mvc.perform(patch("/bookings/1?approved=true"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.booker.email", is(bookingResponseDto.getBooker().getEmail())))
                .andExpect(jsonPath("$.booker.name", is(bookingResponseDto.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(bookingResponseDto.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(bookingResponseDto.getItem().getAvailable())))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())));

        when(httpClientService.get(eq("/bookings/1"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(bookingResponseDto));

        mvc.perform(get("/bookings/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.booker.email", is(bookingResponseDto.getBooker().getEmail())))
                .andExpect(jsonPath("$.booker.name", is(bookingResponseDto.getBooker().getName())))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$.item.description", is(bookingResponseDto.getItem().getDescription())))
                .andExpect(jsonPath("$.item.available", is(bookingResponseDto.getItem().getAvailable())))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())));

        when(httpClientService.get(eq("/bookings?state=ALL"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingResponseDto)));

        mvc.perform(get("/bookings"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$[0].booker.email", is(bookingResponseDto.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].booker.name", is(bookingResponseDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$[0].item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description", is(bookingResponseDto.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available", is(bookingResponseDto.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].start", is(bookingResponseDto.getStart().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].end", is(bookingResponseDto.getEnd().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus().toString())));

        when(httpClientService.get(eq("/bookings/owner?state=ALL"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(bookingResponseDto)));

        mvc.perform(get("/bookings/owner"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].booker.id", is(bookingResponseDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$[0].booker.email", is(bookingResponseDto.getBooker().getEmail())))
                .andExpect(jsonPath("$[0].booker.name", is(bookingResponseDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].item.id", is(bookingResponseDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$[0].item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$[0].item.description", is(bookingResponseDto.getItem().getDescription())))
                .andExpect(jsonPath("$[0].item.available", is(bookingResponseDto.getItem().getAvailable())))
                .andExpect(jsonPath("$[0].start", is(bookingResponseDto.getStart().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].end", is(bookingResponseDto.getEnd().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus().toString())));
    }

    @Test
    void withoutHeaderRequests() throws Exception {
        MockMvc mvcWithoutHeader = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .build();

        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(OffsetDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(OffsetDateTime.now().plusDays(2));
        mvcWithoutHeader.perform(post("/bookings").content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));

        mvcWithoutHeader.perform(patch("/bookings/1?approved=true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));

        mvcWithoutHeader.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));

        mvcWithoutHeader.perform(get("/bookings"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));

        mvcWithoutHeader.perform(get("/bookings/owner"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));
    }
}