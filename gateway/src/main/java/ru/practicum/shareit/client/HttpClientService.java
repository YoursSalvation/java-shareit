package ru.practicum.shareit.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HttpClientService {

    private final RestTemplate restTemplate;

    @Value("${shareit.api.auth.userheader}")
    private String userIdHeader;

    public ResponseEntity<Object> get(String endPoint, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<String> request = new HttpEntity<>("", headers);
        return restTemplate.exchange(endPoint, HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> post(String endPoint, Long userId, Object object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(object, headers);
        return restTemplate.exchange(endPoint, HttpMethod.POST, request, Object.class);
    }

    public ResponseEntity<Object> patch(String endPoint, Long userId, Object object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(object, headers);
        return restTemplate.exchange(endPoint, HttpMethod.PATCH, request, Object.class);
    }

    public ResponseEntity<Object> delete(String endPoint, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<String> request = new HttpEntity<>("", headers);
        return restTemplate.exchange(endPoint, HttpMethod.DELETE, request, Object.class);
    }


}