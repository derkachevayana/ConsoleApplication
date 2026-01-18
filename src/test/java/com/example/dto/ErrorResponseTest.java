package com.example.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void serialization_WithDetails_IncludesAllFields() throws Exception {
        Map<String, String> details = Map.of(
                "email", "Invalid email format",
                "age", "Must be positive"
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.of(2024, 1, 15, 10, 30))
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .path("/api/users")
                .details(details)
                .build();

        String json = objectMapper.writeValueAsString(errorResponse);

        assertThat(json).contains("\"status\":400");
        assertThat(json).contains("\"error\":\"Bad Request\"");
        assertThat(json).contains("\"email\":\"Invalid email format\"");
        assertThat(json).contains("\"age\":\"Must be positive\"");
    }

    @Test
    void serialization_WithoutDetails_ExcludesDetailsField() throws Exception {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .path("/api/users/123")
                .build();

        String json = objectMapper.writeValueAsString(errorResponse);

        assertThat(json).doesNotContain("details");
        assertThat(json).contains("\"status\":404");
        assertThat(json).contains("\"message\":\"Resource not found\"");
    }
}
