package com.pbop.config;

import com.pbop.dtos.errors.ApiErrorDto;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ApiErrorDto> handleRuntimeException(RuntimeException exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // Start with 500
        String errorType = "Internal Server Error";
        String message = exception.getMessage();

        ResponseStatus statusAnnotation = exception.getClass().getAnnotation(ResponseStatus.class);

        if (statusAnnotation != null) {
            // 1. If annotation is found, override the default 500 status
            status = statusAnnotation.value();

            // 2. Override the error type/reason if provided in the annotation (optional)
            errorType = status.getReasonPhrase();
        }
        ApiErrorDto errorDto = new ApiErrorDto(
                status.value(),
                errorType.toUpperCase().replace(" ", "_"),
                message,
                LocalDateTime.now()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(errorDto, headers, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        Throwable rootCause = exception.getRootCause();
        String dbErrorMessage = rootCause != null ? rootCause.getMessage() : "Unknown database error.";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("DEBUGGING DB ERROR: " + dbErrorMessage);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorDto> handleJwtException(JwtException exception) {

        HttpStatus status = HttpStatus.UNAUTHORIZED; // <-- Set the specific 401 status
        String errorType = "Unauthorized";


        ApiErrorDto errorDto = new ApiErrorDto(
                status.value(),
                errorType.toUpperCase(),

                "Authentication failed: Token is expired, invalid, or malformed. Details: " + exception.getMessage(),
                LocalDateTime.now()
        );


        log.info("JWT Authentication Failed: {}", exception.getMessage());


        return new ResponseEntity<>(errorDto, status);
    }
}
