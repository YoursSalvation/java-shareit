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
import ru.practicum.shareit.item.CommentCreateDto;
import ru.practicum.shareit.item.ItemCreateDto;
import ru.practicum.shareit.item.ItemUpdateDto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
}