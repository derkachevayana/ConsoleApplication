package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import com.example.validation.AgeValidator;
import com.example.validation.StringValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailValidator emailValidator;
    @Autowired private StringValidator stringValidator;
    @Autowired private AgeValidator ageValidator;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ValidData_ShouldCreateUser() {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAge(30);

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getAge()).isEqualTo(30);
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void updateUser_PartialUpdate_ShouldUpdateOnlyName() {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Original");
        createRequest.setEmail("original@example.com");
        createRequest.setAge(25);

        UserResponse created = userService.createUser(createRequest);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated");

        UserResponse updated = userService.updateUser(created.getId(), updateRequest);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getEmail()).isEqualTo("original@example.com");
        assertThat(updated.getAge()).isEqualTo(25);
    }

    @Test
    void updateUser_NonExistingUser_ShouldThrowException() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated");

        assertThatThrownBy(() -> userService.updateUser(999L, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void validators_AreAvailableInContext() {
        assertThat(emailValidator).isNotNull();
        assertThat(stringValidator).isNotNull();
        assertThat(ageValidator).isNotNull();
        assertThat(userService).isNotNull();
    }
}
