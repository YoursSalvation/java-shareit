package ru.practicum.shareit.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // VALIDATION ERRORS

    @ExceptionHandler(
            MethodArgumentNotValidException.class                 // @Valid annotation exceptions
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        Object target = e.getBindingResult().getTarget();
        log.debug("VALIDATION FAILED: {} for {}", errorMessage, target);
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST)
                .error("Validation Failed")
                .message(errorMessage)
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler({
            ConstraintViolationException.class,                    // Custom annotation exceptions
            IllegalArgumentException.class,                        // wrong arguments like -1
            MethodArgumentTypeMismatchException.class,             // argument type mismatch
            HttpMessageNotReadableException.class                  // wrong json in request body
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(RuntimeException e, HttpServletRequest request) {
        log.debug("ILLEGAL ARGUMENT: {}", e.getMessage());
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST)
                .error("Illegal Argument")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class                    // missing request header
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeader(Exception e, HttpServletRequest request) {
        log.debug("MISSING HEADER: {}", e.getMessage());
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST)
                .error("Missing Header")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // CONFLICT ERRORS

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException e, HttpServletRequest request) {
        log.debug("CONFLICT: {}", e.getMessage());
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT)
                .error("Conflict")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // FORBIDDEN ERRORS

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenException(ForbiddenException e, HttpServletRequest request) {
        log.debug("FORBIDDEN: {}", e.getMessage());
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN)
                .error("Forbidden action")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // NOT FOUND ERRORS

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e, HttpServletRequest request) {
        log.debug("NOT FOUND: {}", e.getMessage());
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND)
                .error("Not Found")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
    }

}