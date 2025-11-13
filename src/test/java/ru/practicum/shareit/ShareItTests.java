package ru.practicum.shareit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.api.ItemResponseDto;
import ru.practicum.shareit.user.api.UserResponseDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShareItTests {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Value("${shareit.api.auth.userheader}")
    private String userIdHeader;

    protected boolean checkPost(String endPoint, String json, int code, String search) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<String> response = restTemplate.exchange(endPoint, HttpMethod.POST, request, String.class);
        if (code != response.getStatusCode().value()) return false;
        if (search == null) return true;
        if (response.getBody() == null) return false;
        return response.getBody().contains(search);
    }

    protected Long makeUserAndReturnId(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<UserResponseDto> response = restTemplate.exchange("/users", HttpMethod.POST, request,
                UserResponseDto.class);
        assertNotNull(response.getBody());
        return response.getBody().getId();
    }

    protected Long makeItemAndReturnId(String json, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<ItemResponseDto> response = restTemplate.exchange("/items", HttpMethod.POST, request,
                ItemResponseDto.class);
        assertNotNull(response.getBody());
        return response.getBody().getId();
    }

}
