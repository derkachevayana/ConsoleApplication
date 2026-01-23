package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.User;
import com.example.exception.EmailValidationException;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.kafka.UserEventProducer;
import com.example.kafka.UserEventType;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    @Transactional
    public UserResponse createUser(UserRequest request) {

        String email = request.getEmail() != null ?
                request.getEmail().trim().toLowerCase() : "";
        String name = request.getName() != null ?
                request.getName().trim() : "";

        User user = User.builder()
                    .name(name)
                    .email(email)
                    .age(request.getAge())
                    .build();

            try {
                User savedUser = userRepository.save(user);

                userEventProducer.sendUserEvent(
                        UserEventType.USER_CREATED,
                        savedUser.getEmail(),
                        savedUser.getId(),
                        savedUser.getName()
                );

                log.info("Created user with id: {}, email: {}", savedUser.getId(), savedUser.getEmail());
                return mapToResponse(savedUser);

            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation for email {}: {}", email, e.getMessage(), e);
                throw new UserAlreadyExistsException("Email already exists: " + email);
            } catch (Exception e) {
                log.error("Unexpected error creating user with email {}: {}", email, e.getMessage(), e);
                throw e;
            }
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(this::mapToResponse)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id)
                );

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            user.setEmail(newEmail);
        }

        if (request.getAge() != null) {
            if (request.getAge() < 0 || request.getAge() > 120) {
                throw new IllegalArgumentException("Age must be between 0 and 120");
            }
            user.setAge(request.getAge());
        }

        try {
            User updatedUser = userRepository.save(user);
            log.info("Updated user with id: {}", id);
            return mapToResponse(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation during update for user id {}: {}",
                    id, e.getMessage(), e);
            throw new UserAlreadyExistsException("Email already exists");
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        userRepository.delete(user);

        userEventProducer.sendUserEvent(
                UserEventType.USER_DELETED,
                user.getEmail(),
                user.getId(),
                user.getName()
        );

        log.info("Deleted user with id: {}, email: {}", id, user.getEmail());
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
