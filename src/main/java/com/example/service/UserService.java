package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse getUserByEmail(String email);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
