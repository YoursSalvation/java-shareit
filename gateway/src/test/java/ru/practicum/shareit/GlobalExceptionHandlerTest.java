package ru.practicum.shareit;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    void handleHttpClientError() {
        HttpStatus statusCode = HttpStatus.BAD_REQUEST;
        String responseBody = "Error message";
        HttpClientErrorException exception = mock(HttpClientErrorException.class);
        when(exception.getStatusCode()).thenReturn(statusCode);
        when(exception.getResponseBodyAs(Object.class)).thenReturn(responseBody);

        ResponseEntity<Object> result = globalExceptionHandler.handleHttpClientError(exception);
        assertEquals(statusCode, result.getStatusCode());
        assertEquals(responseBody, result.getBody());
    }

    @Test
    void handleHttpHostConnectException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Connection refused";
        HttpHostConnectException exception = new HttpHostConnectException(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleHttpHostConnectException(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    void handleIllegalArgumentException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Illegal argument provided";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleIllegalArgument(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    void handleBadRequest() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Bad request occurred";
        BadRequestException exception = new BadRequestException(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleBadRequest(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    void handleForbiddenException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Access denied";
        ForbiddenException exception = new ForbiddenException(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleForbiddenException(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    void handleNotFoundException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Resource not found";
        NotFoundException exception = new NotFoundException(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleNotFoundException(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }
}