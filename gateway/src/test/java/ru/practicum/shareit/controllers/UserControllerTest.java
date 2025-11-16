package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.practicum.shareit.user.UserCreateDto;
import ru.practicum.shareit.user.UserUpdateDto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockitoBean
    private HttpClientService httpClientService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(MockMvcRequestBuilders.get("")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .build();
    }

    @Test
    void badRequests() throws Exception {
        mvc.perform(post("/users").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(post("/users").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        mvc.perform(patch("/users/1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(patch("/users/1").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        mvc.perform(get("/users/f").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(get("/users/-1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(get("/users/0").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));

        mvc.perform(delete("/users/f").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(delete("/users/-1").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
        mvc.perform(delete("/users/0").content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Illegal Argument")));
    }

    @Test
    void nameValidation() throws Exception {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setEmail("name.val@test.ru");
        mvc.perform(post("/users").content(mapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        userCreateDto.setName("");
        mvc.perform(post("/users").content(mapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("");
        mvc.perform(patch("/users/1").content(mapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void emailValidation() throws Exception {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("no matter");
        mvc.perform(post("/users").content(mapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        userCreateDto.setEmail("");
        mvc.perform(post("/users").content(mapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("");
        mvc.perform(patch("/users/1").content(mapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void notFoundRequests() throws Exception {
        ErrorResponse notFoundResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND)
                .error("Not Found")
                .message("User 1 not found")
                .path("/users")
                .build();
        ResponseEntity<Object> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);

        when(httpClientService.get(eq("/users/1"), eq(null))).thenReturn(responseEntity);
        mvc.perform(get("/users/1").content(""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        when(httpClientService.delete(eq("/users/1"), eq(null))).thenReturn(responseEntity);
        mvc.perform(delete("/users/1").content(""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        when(httpClientService.patch(eq("/users/1"), eq(null), any())).thenReturn(responseEntity);
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("not.found2@test.ru");
        mvc.perform(patch("/users/1").content(mapper.writeValueAsString(userUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }
}