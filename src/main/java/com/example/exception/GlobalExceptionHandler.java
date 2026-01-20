package com.example.exception;

import com.example.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("User already exists: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(EmailValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleEmailValidation(EmailValidationException ex, HttpServletRequest request) {
        log.warn("Email validation failed: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex,
                                          HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed for {}: {}", request.getRequestURI(), errors);
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed",
                errors, request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                      HttpServletRequest request) {
        log.error("Data integrity violation for {}: {}", request.getRequestURI(), ex.getMessage());

        String errorMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        String message;

        if (errorMessage.contains("email") ||
                errorMessage.contains("unique") ||
                errorMessage.contains("duplicate") ||
                errorMessage.contains("constraint")) {
            message = "Email already exists";
        } else {
            message = "Data integrity violation";
        }

        return ErrorResponse.of(HttpStatus.CONFLICT, message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex,
                                                HttpServletRequest request) {
        log.error("Internal server error: {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error", request.getRequestURI());
    }
}
