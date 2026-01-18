package com.example.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Nullable
    private String name;

    @Nullable
    private String email;

    @Nullable
    private Integer age;
}
