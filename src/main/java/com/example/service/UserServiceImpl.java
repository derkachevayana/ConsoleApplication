package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.exception.ConstraintViolationAnalyzer;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import com.example.validation.AgeValidator;
import com.example.validation.StringValidator;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ConstraintViolationAnalyzer constraintAnalyzer;
    private final EmailValidator emailValidator;
    private final StringValidator stringValidator;
    private final AgeValidator ageValidator;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        String normalizedEmail = emailValidator.validateAndNormalize(request.getEmail());
        String normalizedName = stringValidator.validateName(request.getName(), true).getValue();
        Integer age = ageValidator.validateAge(request.getAge(), true).getValue();

        User user = User.builder()
                    .name(normalizedName)
                    .email(normalizedEmail)
                    .age(age)
                    .build();

            try {
                User savedUser = userRepository.save(user);
                return mapToResponse(savedUser);
            } catch (DataIntegrityViolationException ex) {
                throw constraintAnalyzer.convertToBusinessException(ex, normalizedEmail);
        }
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        String trimmedEmail = email.trim();
        return userRepository.findByEmail(trimmedEmail)
                .map(this::mapToResponse)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id)
                );

        updateEmailIfProvided(user, request.getEmail());
        updateNameIfProvided(user, request.getName());
        updateAgeIfProvided(user, request.getAge());

        try {
            User updatedUser = userRepository.save(user);
            return mapToResponse(updatedUser);
        } catch (DataIntegrityViolationException ex) {
            String email = request.getEmail() != null ? request.getEmail() : "";
            throw constraintAnalyzer.convertToBusinessException(ex, email);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private void updateEmailIfProvided(User user, @Nullable String newEmail) {
        if (newEmail != null) {
            EmailValidator.ValidationResult result = emailValidator.validateEmail(newEmail, false);
            if (!result.isValid()) {
                throw new IllegalArgumentException(result.getError());
            }
            if (result.hasValue() && !result.getValue().equals(user.getEmail())) {
                user.setEmail(result.getValue());
            }
        }
    }

    private void updateNameIfProvided(User user, @Nullable String newName) {
        if (newName != null) {
            StringValidator.ValidationResult result = stringValidator.validateName(newName, false);
            if (!result.isValid()) {
                throw new IllegalArgumentException(result.getError());
            }
            if (result.hasValue()) {
                user.setName(result.getValue());
            }
        }
    }

    private void updateAgeIfProvided(User user, @Nullable Integer newAge) {
        if (newAge != null) {
            AgeValidator.ValidationResult result = ageValidator.validateAge(newAge, false);
            if (!result.isValid()) {
                throw new IllegalArgumentException(result.getError());
            }
            if (result.hasValue()) {
                user.setAge(result.getValue());
            }
        }
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
