package ru.practicum.shareit.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Builder
@Data
public class ErrorResponse {

  private Instant timestamp;
  private HttpStatus status;
  private String error;
  private String message;
  private String path;

}