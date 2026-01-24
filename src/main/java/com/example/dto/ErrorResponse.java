package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Структура ошибки API")
public class ErrorResponse {

    @Schema(description = "Время возникновения ошибки", example = "2025-01-23T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус код", example = "400")
    private int status;

    @Schema(description = "Описание ошибки", example = "Bad Request")
    private String error;

    @Schema(description = "Сообщение об ошибке", example = "Invalid email format")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/users")
    private String path;

    @Schema(description = "Детали ошибки")
    private Map<String, String> details;

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(HttpStatus status, String message,
                                   Map<String, String> details, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .details(details)
                .path(path)
                .build();
    }
}
