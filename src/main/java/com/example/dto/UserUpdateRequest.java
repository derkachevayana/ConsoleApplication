package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление пользователя")
public class UserUpdateRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Имя пользователя", example = "Петр Петров")
    private String name;

    @Email(message = "Invalid email format")
    @Schema(description = "Email пользователя", example = "petr@example.com")
    private String email;

    @Min(value = 0, message = "Age must be positive or zero")
    @Max(value = 120, message = "Age must be less than or equal to 120")
    @Schema(description = "Возраст пользователя", example = "30")
    private Integer age;
}
