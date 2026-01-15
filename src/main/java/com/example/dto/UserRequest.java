package com.example.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UserRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Pattern( // я пыталась использовать "@email" но он плохо валидирует вводимый емейл. Поэтому тут применила проверку по регулярному выражению
            regexp = "^[\\p{L}0-9+_.%-]+@[\\p{L}0-9.-]+\\.[\\p{L}]{2,}$",
            message = "Email must contain @ and a dot in domain (e.g., user@example.com)"
    )
    private String email;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be positive or zero")
    @Max(value = 120, message = "Age must be less than or equal to 120")
    private Integer age;
}
