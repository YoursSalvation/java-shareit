package ru.practicum.shareit.request;

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
import ru.practicum.shareit.item.ItemResponseDtoForItemRequests;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    @MockitoBean
    private ItemRequestService itemRequestService;

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
        when(itemRequestService.getRequestById(eq(1L))).thenThrow(new NotFoundException("Not found"));
        mvc.perform(get("/requests/1").content(""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
        verify(itemRequestService, times(1)).getRequestById(any());
    }

    @Test
    void crudGoodScenario() throws Exception {
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

        when(itemRequestService.create(eq(1L), any())).thenReturn(itemRequestResponseDto);
        mvc.perform(post("/requests").content(mapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())));
        verify(itemRequestService, times(1)).create(any(), any());

        when(itemRequestService.getRequestById(eq(1L))).thenReturn(itemRequestResponseDto);
        mvc.perform(get("/requests/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.items[0].id", is(itemRequestResponseDto.getItems().getFirst().getId().intValue())))
                .andExpect(jsonPath("$.items[0].name", is(itemRequestResponseDto.getItems().getFirst().getName())))
                .andExpect(jsonPath("$.items[0].ownerId", is(itemRequestResponseDto.getItems().getFirst().getOwnerId().intValue())));
        verify(itemRequestService, times(1)).getRequestById(any());

        when(itemRequestService.getMyRequests(eq(1L))).thenReturn(List.of(itemRequestResponseDto));
        mvc.perform(get("/requests"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].items[0].id", is(itemRequestResponseDto.getItems().getFirst().getId().intValue())))
                .andExpect(jsonPath("$[0].items[0].name", is(itemRequestResponseDto.getItems().getFirst().getName())))
                .andExpect(jsonPath("$[0].items[0].ownerId", is(itemRequestResponseDto.getItems().getFirst().getOwnerId().intValue())));
        verify(itemRequestService, times(1)).getMyRequests(any());

        when(itemRequestService.getOthersRequests(eq(1L))).thenReturn(List.of(itemRequestResponseSimpleViewDto));
        mvc.perform(get("/requests/all"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))));
        verify(itemRequestService, times(1)).getOthersRequests(any());

    }
}