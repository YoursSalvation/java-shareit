package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemResponseDto;
import ru.practicum.shareit.item.ItemUpdateDto;
import ru.practicum.shareit.user.UserResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @MockitoBean
    private BookingService bookingService;

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
    void notFoundRequests() throws Exception {
        when(bookingService.getById(eq(1L), eq(1L))).thenThrow(new NotFoundException("Not found"));
        mvc.perform(get("/bookings/1").content(""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
        verify(bookingService, times(1)).getById(any(), any());

        when(bookingService.approveReject(eq(1L), eq(1L), eq(false))).thenThrow(
                new NotFoundException("Not found"));
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("new name");
        mvc.perform(patch("/bookings/1?approved=false").content(mapper.writeValueAsString(itemUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
        verify(bookingService, times(1)).approveReject(any(), any(), anyBoolean());
    }

    @Test
    void wellWork() throws Exception {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(OffsetDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(OffsetDateTime.now().plusDays(2));

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setEmail("abcd@efg.com");
        userResponseDto.setName("abcd");

        ItemResponseDto itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("onetwo");
        itemResponseDto.setDescription("one, two");
        itemResponseDto.setAvailable(true);

        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(1L);
        bookingResponseDto.setBooker(userResponseDto);
        bookingResponseDto.setItem(itemResponseDto);
        bookingResponseDto.setStart(OffsetDateTime.now().plusDays(1));
        bookingResponseDto.setEnd(OffsetDateTime.now().plusDays(2));
        bookingResponseDto.setStatus(BookingStatus.WAITING);

        when(bookingService.create(eq(1L), any())).thenReturn(bookingResponseDto);
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
        verify(bookingService, times(1)).create(any(), any());

        when(bookingService.approveReject(eq(1L), eq(1L), eq(true))).thenReturn(bookingResponseDto);
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
        verify(bookingService, times(1)).approveReject(any(), any(), anyBoolean());

        when(bookingService.getById(eq(1L), eq(1L))).thenReturn(bookingResponseDto);
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
        verify(bookingService, times(1)).getById(any(), any());

        when(bookingService.getBookerBookings(eq(1L), eq(BookingApiState.ALL))).thenReturn(List.of(bookingResponseDto));
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
        verify(bookingService, times(1)).getBookerBookings(any(), any());

        when(bookingService.getOwnerBookings(eq(1L), eq(BookingApiState.ALL))).thenReturn(List.of(bookingResponseDto));
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
        verify(bookingService, times(1)).getOwnerBookings(any(), any());
    }
}