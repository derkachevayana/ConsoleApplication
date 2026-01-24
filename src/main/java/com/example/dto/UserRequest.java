package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
@Schema(description = "Запрос на создание пользователя")
public class UserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Имя пользователя", example = "Иван Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email пользователя", example = "ivan@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be positive or zero")
    @Max(value = 120, message = "Age must be less than or equal to 120")
    @Schema(description = "Возраст пользователя", example = "25",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer age;
}
