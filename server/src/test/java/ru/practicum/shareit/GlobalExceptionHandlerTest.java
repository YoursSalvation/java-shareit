package ru.practicum.shareit;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.exception.*;

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