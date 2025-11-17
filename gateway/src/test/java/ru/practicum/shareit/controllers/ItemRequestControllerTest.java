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
import ru.practicum.shareit.client.HttpClientService;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.item.ItemResponseDtoForItemRequests;
import ru.practicum.shareit.request.ItemRequestCreateDto;
import ru.practicum.shareit.request.ItemRequestResponseDto;
import ru.practicum.shareit.request.ItemRequestResponseSimpleViewDto;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

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
        mvc.perform(post("/requests").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(post("/requests").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        mvc.perform(get("/requests/f").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(get("/requests/-1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(get("/requests/0").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
    }

    @Test
    void descriptionValidation() throws Exception {
        // POST
        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        mvc.perform(post("/requests").content(mapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        itemRequestCreateDto.setDescription(" ");
        mvc.perform(post("/requests").content(mapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void notFoundRequests() throws Exception {
        ErrorResponse notFoundResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND)
                .error("Not Found")
                .message("Item Request 1 not found")
                .path("/requests")
                .build();
        ResponseEntity<Object> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);

        when(httpClientService.get(eq("/requests/1"), eq(1L))).thenReturn(responseEntity);
        mvc.perform(get("/requests/1").content(""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void wellWork() throws Exception {
        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("desc");

        ItemResponseDtoForItemRequests itemResponseDtoForItemRequests = new ItemResponseDtoForItemRequests();
        itemResponseDtoForItemRequests.setId(1L);
        itemResponseDtoForItemRequests.setName("name");
        itemResponseDtoForItemRequests.setOwnerId(3L);

        ItemRequestResponseDto itemRequestResponseDto = new ItemRequestResponseDto();
        itemRequestResponseDto.setId(1L);
        itemRequestResponseDto.setDescription("desc");
        itemRequestResponseDto.setItems(List.of(itemResponseDtoForItemRequests));
        itemRequestResponseDto.setCreated(OffsetDateTime.now().minusDays(1));

        ItemRequestResponseSimpleViewDto itemRequestResponseSimpleViewDto = new ItemRequestResponseSimpleViewDto();
        itemRequestResponseSimpleViewDto.setId(1L);
        itemRequestResponseSimpleViewDto.setDescription("desc");
        itemRequestResponseSimpleViewDto.setCreated(OffsetDateTime.now().minusDays(1));

        when(httpClientService.post(eq("/requests"), eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemRequestResponseDto));

        mvc.perform(post("/requests").content(mapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())));

        when(httpClientService.get(eq("/requests/1"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemRequestResponseDto));

        mvc.perform(get("/requests/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.items[0].id", is(itemRequestResponseDto.getItems().getFirst().getId().intValue())))
                .andExpect(jsonPath("$.items[0].name", is(itemRequestResponseDto.getItems().getFirst().getName())))
                .andExpect(jsonPath("$.items[0].ownerId", is(itemRequestResponseDto.getItems().getFirst().getOwnerId().intValue())));

        when(httpClientService.get(eq("/requests"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemRequestResponseDto)));

        mvc.perform(get("/requests"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].items[0].id", is(itemRequestResponseDto.getItems().getFirst().getId().intValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(itemRequestResponseDto.getItems().getFirst().getName())))
                .andExpect(jsonPath("$[0].items[0].ownerId", is(itemRequestResponseDto.getItems().getFirst().getOwnerId().intValue())));

        when(httpClientService.get(eq("/requests/all"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemRequestResponseSimpleViewDto)));

        mvc.perform(get("/requests/all"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))));
    }

    @Test
    void withoutHeaderRequests() throws Exception {
        MockMvc mvcWithoutHeader = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .build();

        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto();
        itemRequestCreateDto.setDescription("desc");
        mvcWithoutHeader.perform(post("/requests").content(mapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));

        mvcWithoutHeader.perform(get("/requests/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));

        mvcWithoutHeader.perform(get("/requests"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));

        mvcWithoutHeader.perform(get("/requests/all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Missing Header")));
    }
}