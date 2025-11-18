package ru.practicum.shareit.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class HttpClientServiceTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private HttpClientService httpClientService;

    @Test
    void get() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("GET OK"));

        ResponseEntity<Object> response = httpClientService.get("/test", 1L);
        assertEquals("GET OK", response.getBody());

        response = httpClientService.get("/test", null);
        assertEquals("GET OK", response.getBody());
    }

    @Test
    void post() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("POST OK"));

        ResponseEntity<Object> response = httpClientService.post("/test", 1L, "");
        assertEquals("POST OK", response.getBody());

        response = httpClientService.post("/test", null, "");
        assertEquals("POST OK", response.getBody());
    }

    @Test
    void patch() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("PATCH OK"));

        ResponseEntity<Object> response = httpClientService.patch("/test", 1L, "");
        assertEquals("PATCH OK", response.getBody());

        response = httpClientService.patch("/test", null, "");
        assertEquals("PATCH OK", response.getBody());
    }

    @Test
    void delete() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("DELETE OK"));

        ResponseEntity<Object> response = httpClientService.delete("/test", 1L);
        assertEquals("DELETE OK", response.getBody());

        response = httpClientService.delete("/test", null);
        assertEquals("DELETE OK", response.getBody());
    }

}