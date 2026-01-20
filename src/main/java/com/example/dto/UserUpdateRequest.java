package com.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String email;

    @Min(value = 0, message = "Age must be positive or zero")
    @Max(value = 120, message = "Age must be less than or equal to 120")
    private Integer age;
}
