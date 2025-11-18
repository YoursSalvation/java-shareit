package ru.practicum.shareit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.exception.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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
    void handleMethodArgumentNotValidException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Validation failed";
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", errorMessage);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.List.of(fieldError));
        when(bindingResult.getTarget()).thenReturn(new Object());

        ErrorResponse result = globalExceptionHandler.handleMethodArgumentNotValidException(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    void handleConstraintViolationException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Constraint violation";
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(errorMessage);
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ErrorResponse result = globalExceptionHandler.handleIllegalArgument(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertTrue(result.getMessage().contains(errorMessage));
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
    void handleMethodArgumentTypeMismatchException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Type mismatch";
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getMessage()).thenReturn(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleIllegalArgument(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    void handleHttpMessageNotReadableException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "JSON parse error";
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleIllegalArgument(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals(errorMessage, result.getMessage());
    }

    @Test
    void handleMissingServletRequestParameterException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Missing parameter";
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException("param", "String");

        ErrorResponse result = globalExceptionHandler.handleIllegalArgument(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertTrue(result.getMessage().contains("param"));
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
    void handleConflictException() {
        when(httpServletRequest.getRequestURI()).thenReturn("/test-path");
        String errorMessage = "Resource already exists";
        ConflictException exception = new ConflictException(errorMessage);

        ErrorResponse result = globalExceptionHandler.handleConflictException(exception, httpServletRequest);
        assertNotNull(result);
        assertEquals(HttpStatus.CONFLICT, result.getStatus());
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