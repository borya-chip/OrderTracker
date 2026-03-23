package com.order.tracker.exception;

import com.order.tracker.exception.response.ErrorResponse;
import com.order.tracker.exception.response.ValidationErrorResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String MALFORMED_JSON_REQUEST = "Malformed JSON request";
    private static final String UNEXPECTED_ERROR = "An unexpected error occurred";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(final ApiException exception) {
        HttpStatus status = exception.getStatus();
        logApiException(status, exception);
        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("{}: {}", VALIDATION_FAILED, errors);
        return ResponseEntity.badRequest()
                .body(buildValidationErrorResponse(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            final MethodArgumentTypeMismatchException exception) {
        String message = "Invalid value '%s' for parameter '%s'"
                .formatted(exception.getValue(), exception.getName());
        log.warn("Method argument type mismatch: {}", message);
        return ResponseEntity.badRequest()
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            final HttpMessageNotReadableException exception) {
        Throwable cause = exception.getMostSpecificCause();
        String message = MALFORMED_JSON_REQUEST;
        if (cause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getPath() != null
                && !invalidFormatException.getPath().isEmpty()) {
            String fieldName = invalidFormatException.getPath().getFirst().getPropertyName();
            message = "Invalid value for field '%s'".formatted(fieldName);
        }
        log.warn("{}: {}", MALFORMED_JSON_REQUEST, message);
        return ResponseEntity.badRequest()
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR));
    }

    private void logApiException(final HttpStatus status, final ApiException exception) {
        if (status.is5xxServerError()) {
            log.error("API exception [{}]: {}", status.value(), exception.getMessage());
            return;
        }
        log.warn("API exception [{}]: {}", status.value(), exception.getMessage());
    }

    private ErrorResponse buildErrorResponse(final HttpStatus status, final String message) {
        return new ErrorResponse(status.value(), message, LocalDateTime.now());
    }

    private ValidationErrorResponse buildValidationErrorResponse(
            final HttpStatus status,
            final String message,
            final Map<String, String> errors) {
        return new ValidationErrorResponse(status.value(), message, LocalDateTime.now(), errors);
    }
}
