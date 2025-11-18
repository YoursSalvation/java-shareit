package ru.practicum.shareit.item;

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

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @MockitoBean
    private ItemService itemService;

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
        when(itemService.getById(eq(1L), eq(1L))).thenThrow(new NotFoundException("Item not found"));
        mvc.perform(get("/items/1").content(""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
        verify(itemService, times(1)).getById(any(), any());

        doThrow(new NotFoundException("Item not found")).when(itemService).deleteById(eq(1L), eq(1L));
        mvc.perform(delete("/items/1").content(""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
        verify(itemService, times(1)).deleteById(any(), any());

        when(itemService.update(eq(1L), eq(1L), any())).thenThrow(new NotFoundException("Item not found"));
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("new name");
        mvc.perform(patch("/items/1").content(mapper.writeValueAsString(itemUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
        verify(itemService, times(1)).update(any(), any(), any());
    }

    @Test
    void wellWork() throws Exception {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("name");
        itemCreateDto.setDescription("desc");
        itemCreateDto.setAvailable(true);

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("new name");

        ItemResponseDto itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("name");
        itemResponseDto.setDescription("desc");
        itemResponseDto.setAvailable(true);

        ItemResponseExtendedViewDto itemResponseExtendedViewDto = new ItemResponseExtendedViewDto();
        itemResponseExtendedViewDto.setId(1L);
        itemResponseExtendedViewDto.setName("name");
        itemResponseExtendedViewDto.setDescription("desc");
        itemResponseExtendedViewDto.setAvailable(true);
        itemResponseExtendedViewDto.setLastBooking(OffsetDateTime.now().minusDays(2));
        itemResponseExtendedViewDto.setNextBooking(OffsetDateTime.now().plusDays(2));
        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(4L);
        commentResponseDto.setAuthorName("alex");
        commentResponseDto.setText("sample text");
        commentResponseDto.setCreated(OffsetDateTime.now().minusHours(10));
        itemResponseExtendedViewDto.setComments(List.of(commentResponseDto));

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("lorem ipsum");

        when(itemService.create(eq(1L), any())).thenReturn(itemResponseDto);
        mvc.perform(post("/items").content(mapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemResponseDto.getName())))
                .andExpect(jsonPath("$.description", is(itemResponseDto.getDescription())));
        verify(itemService, times(1)).create(any(), any());

        when(itemService.update(eq(1L), eq(1L), any())).thenReturn(itemResponseDto);
        mvc.perform(patch("/items/1").content(mapper.writeValueAsString(itemUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemResponseDto.getName())))
                .andExpect(jsonPath("$.description", is(itemResponseDto.getDescription())));
        verify(itemService, times(1)).update(any(), any(), any());

        doNothing().when(itemService).deleteById(eq(1L), eq(1L));
        mvc.perform(delete("/items/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
        verify(itemService, times(1)).deleteById(any(), any());

        when(itemService.getById(eq(1L), eq(1L))).thenReturn(itemResponseExtendedViewDto);
        mvc.perform(get("/items/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemResponseExtendedViewDto.getName())))
                .andExpect(jsonPath("$.description", is(itemResponseExtendedViewDto.getDescription())))
                .andExpect(jsonPath("$.lastBooking", is(itemResponseExtendedViewDto.getLastBooking().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.nextBooking", is(itemResponseExtendedViewDto.getNextBooking().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$.comments[0].id", is(itemResponseExtendedViewDto.getComments().getFirst().getId().intValue())))
                .andExpect(jsonPath("$.comments[0].text", is(itemResponseExtendedViewDto.getComments().getFirst().getText())))
                .andExpect(jsonPath("$.comments[0].authorName", is(itemResponseExtendedViewDto.getComments().getFirst().getAuthorName())))
                .andExpect(jsonPath("$.comments[0].created", is(itemResponseExtendedViewDto.getComments().getFirst().getCreated().atZoneSameInstant(zoneId).format(formatter))));
        verify(itemService, times(1)).getById(any(), any());

        when(itemService.findByOwnerId(eq(1L))).thenReturn(List.of(itemResponseExtendedViewDto));
        mvc.perform(get("/items"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemResponseExtendedViewDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemResponseExtendedViewDto.getDescription())))
                .andExpect(jsonPath("$[0].lastBooking", is(itemResponseExtendedViewDto.getLastBooking().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].nextBooking", is(itemResponseExtendedViewDto.getNextBooking().atZoneSameInstant(zoneId).format(formatter))))
                .andExpect(jsonPath("$[0].comments[0].id", is(itemResponseExtendedViewDto.getComments().getFirst().getId().intValue())))
                .andExpect(jsonPath("$[0].comments[0].text", is(itemResponseExtendedViewDto.getComments().getFirst().getText())))
                .andExpect(jsonPath("$[0].comments[0].authorName", is(itemResponseExtendedViewDto.getComments().getFirst().getAuthorName())))
                .andExpect(jsonPath("$[0].comments[0].created", is(itemResponseExtendedViewDto.getComments().getFirst().getCreated().atZoneSameInstant(zoneId).format(formatter))));
        verify(itemService, times(1)).findByOwnerId(any());

        when(itemService.findByText(eq("drill"))).thenReturn(List.of(itemResponseDto));
        mvc.perform(get("/items/search?text=drill"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemResponseDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemResponseDto.getDescription())));
        verify(itemService, times(1)).findByText(any());

        when(itemService.addComment(eq(1L), eq(1L), any())).thenReturn(commentResponseDto);
        mvc.perform(post("/items/1/comment").content(mapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.text", is(commentResponseDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentResponseDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))));
        verify(itemService, times(1)).addComment(any(), any(), any());

    }
}