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
import ru.practicum.shareit.item.*;

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

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

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
        mvc.perform(post("/items").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(post("/items").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        mvc.perform(patch("/items/1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(patch("/items/1").content("hrtfhyrth"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));

        mvc.perform(get("/items/f").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(get("/items/-1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(get("/items/0").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));

        mvc.perform(delete("/items/f").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(delete("/items/-1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(delete("/items/0").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
    }

    @Test
    void nameValidation() throws Exception {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setDescription("no matter");
        itemCreateDto.setAvailable(true);

        mvc.perform(post("/items").content(mapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        itemCreateDto.setName(" ");
        mvc.perform(post("/items").content(mapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void descriptionValidation() throws Exception {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("name");
        itemCreateDto.setAvailable(true);

        mvc.perform(post("/items").content(mapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        itemCreateDto.setDescription(" ");
        mvc.perform(post("/items").content(mapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setDescription(" ");
        mvc.perform(patch("/items/1").content(mapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void availableValidation() throws Exception {
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        itemCreateDto.setName("name");
        itemCreateDto.setDescription("no matter");
        mvc.perform(post("/items").content(mapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        String wrongCreateJson = "{\"name\":\"name\",\"description\":\"desc\",\"available\":\"f\"}";
        mvc.perform(post("/items").content(wrongCreateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));

        String wrongUpdateJson = "{\"available\" : \"f\"}";
        mvc.perform(patch("/items/1").content(wrongUpdateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
    }

    @Test
    void commentTextValidation() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        mvc.perform(post("/items").content(mapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        commentCreateDto.setText(" ");
        mvc.perform(post("/items").content(mapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void notFoundRequests() throws Exception {
        ErrorResponse notFoundResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND)
                .error("Not Found")
                .message("Item 1 not found")
                .path("/items")
                .build();
        ResponseEntity<Object> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);

        when(httpClientService.get(eq("/items/1"), eq(1L))).thenReturn(responseEntity);
        mvc.perform(get("/items/1").content(""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        when(httpClientService.delete(eq("/items/1"), eq(1L))).thenReturn(responseEntity);
        mvc.perform(delete("/items/1").content(""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        when(httpClientService.patch(eq("/items/1"), eq(1L), any())).thenReturn(responseEntity);
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("new name");
        mvc.perform(patch("/items/1").content(mapper.writeValueAsString(itemUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
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
        commentResponseDto.setAuthorName("ivan");
        commentResponseDto.setText("real item");
        commentResponseDto.setCreated(OffsetDateTime.now().minusHours(10));
        itemResponseExtendedViewDto.setComments(List.of(commentResponseDto));

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("need it");

        when(httpClientService.post(eq("/items"), eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemResponseDto));

        mvc.perform(post("/items").content(mapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemResponseDto.getName())))
                .andExpect(jsonPath("$.description", is(itemResponseDto.getDescription())));

        when(httpClientService.patch(eq("/items/1"), eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemResponseDto));

        mvc.perform(patch("/items/1").content(mapper.writeValueAsString(itemUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemResponseDto.getName())))
                .andExpect(jsonPath("$.description", is(itemResponseDto.getDescription())));

        when(httpClientService.delete(eq("/items/1"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(null));

        mvc.perform(delete("/items/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        when(httpClientService.get(eq("/items/1"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(itemResponseExtendedViewDto));

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

        when(httpClientService.get(eq("/items"), eq(1L)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemResponseExtendedViewDto)));

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

        when(httpClientService.get(eq("/items/search?text=saw"), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(List.of(itemResponseDto)));

        mvc.perform(get("/items/search?text=saw"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemResponseDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemResponseDto.getDescription())));

        mvc.perform(get("/items/search?text="))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        when(httpClientService.post(eq("/items/1/comment"), eq(1L), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(commentResponseDto));

        mvc.perform(post("/items/1/comment").content(mapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponseDto.getId().intValue())))
                .andExpect(jsonPath("$.text", is(commentResponseDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentResponseDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentResponseDto.getCreated().atZoneSameInstant(zoneId).format(formatter))));

    }
}