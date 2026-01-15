package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.exception.UserAlreadyExistsException;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {

        String trimmedEmail = request.getEmail().trim();
        String trimmedName = request.getName().trim();

        if (userRepository.existsByEmail(trimmedEmail)) {
            throw new UserAlreadyExistsException("Email already exists: " + trimmedEmail);
        }

        User user = new User();
        user.setName(trimmedName);
        user.setEmail(trimmedEmail);
        user.setAge(request.getAge());

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.isEmpty()) {
                if (!newEmail.equals(user.getEmail())) {
                    if (userRepository.existsByEmail(newEmail)) {
                        throw new UserAlreadyExistsException("Email already exists: " + newEmail);
                    }
                    user.setEmail(newEmail);
                }
            }
        }


        if (request.getName() != null) {
            String newName = request.getName().trim();
            if (!newName.isEmpty()) {
                user.setName(newName);
            }
        }

        if (request.getAge() != null) {
            if (request.getAge() < 0 || request.getAge() > 120) {
                throw new IllegalArgumentException("Age must be between 0 and 120");
            }
            user.setAge(request.getAge());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
