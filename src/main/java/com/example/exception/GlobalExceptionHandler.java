package com.example.exception;

import com.example.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ConstraintViolationAnalyzer constraintAnalyzer;

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex,
                                            HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExists(UserAlreadyExistsException ex,
                                                 HttpServletRequest request) {
        log.warn("User already exists: {}", ex.getMessage());
        return ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex,
                                               HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        if (ex.getMessage() != null && ex.getMessage().contains("Email must contain")) {
            Map<String, String> details = new HashMap<>();
            details.put("email", ex.getMessage());
            return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed",
                    details, request.getRequestURI());
        }

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex,
                                          HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed",
                errors, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex,
                                                   HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Validation failed",
                errors, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                            HttpServletRequest request) {
        log.warn("Type mismatch: {}", ex.getMessage());

        Map<String, String> details = new HashMap<>();
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(), ex.getRequiredType() != null ?
                        ex.getRequiredType().getSimpleName() : "unknown");
        details.put(ex.getName(), message);

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Type mismatch",
                details, request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParams(MissingServletRequestParameterException ex,
                                             HttpServletRequest request) {
        log.warn("Missing parameter: {}", ex.getParameterName());

        Map<String, String> details = new HashMap<>();
        details.put(ex.getParameterName(), "Parameter is required");

        return ErrorResponse.of(HttpStatus.BAD_REQUEST, "Missing required parameter",
                details, request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                      HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        String message = constraintAnalyzer.isEmailConstraintViolation(ex)
                ? "Email already exists"
                : "Data integrity violation";

        return ErrorResponse.of(HttpStatus.CONFLICT, message, request.getRequestURI());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoHandlerFound(NoHandlerFoundException ex,
                                              HttpServletRequest request) {
        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        String message = String.format("No endpoint %s %s",
                ex.getHttpMethod(), ex.getRequestURL());

        return ErrorResponse.of(HttpStatus.NOT_FOUND, message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex,
                                                HttpServletRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error", request.getRequestURI());
    }
}
