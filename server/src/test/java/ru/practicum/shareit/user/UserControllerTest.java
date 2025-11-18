package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @MockitoBean
    private UserService userService;

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
    void notFoundRequests() throws Exception {
        when(userService.update(any(), eq(1L))).thenThrow(new NotFoundException("User 1 not found"));
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("notfound@test.ru");
        mvc.perform(patch("/users/1").content(mapper.writeValueAsString(userUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        doThrow(new NotFoundException("User 1 not found")).when(userService).deleteById(eq(1L));
        mvc.perform(delete("/users/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));

        when(userService.getById(eq(1L))).thenThrow(new NotFoundException("User 1 not found"));
        mvc.perform(get("/users/1").content(""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    void wellWork() throws Exception {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setEmail("eaddress@test.ru");
        userCreateDto.setName("alexander");

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setEmail("eaddress@test.ru");
        userResponseDto.setName("alexander");

        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("new@email.com");
        userResponseDto.setEmail("new@email.com");

        when(userService.create(any())).thenReturn(userResponseDto);
        mvc.perform(post("/users").content(mapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(userResponseDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userResponseDto.getName())));
        verify(userService, times(1)).create(any());

        when(userService.update(any(), eq(1L))).thenReturn(userResponseDto);
        mvc.perform(patch("/users/1").content(mapper.writeValueAsString(userUpdateDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(userResponseDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userResponseDto.getName())));
        verify(userService, times(1)).update(any(), any());

        when(userService.getById(eq(1L))).thenReturn(userResponseDto);
        mvc.perform(get("/users/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(userResponseDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userResponseDto.getName())));
        verify(userService, times(1)).getById(any());

        when(userService.getList()).thenReturn(List.of(userResponseDto));
        mvc.perform(get("/users"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email", is(userResponseDto.getEmail())))
                .andExpect(jsonPath("$[0].name", is(userResponseDto.getName())));
        verify(userService, times(1)).getList();

        doNothing().when(userService).deleteById(eq(1L));
        mvc.perform(delete("/users/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
        verify(userService, times(1)).deleteById(any());
    }
}