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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.client.HttpClientService;
import ru.practicum.shareit.exception.ErrorResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.is;
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
}