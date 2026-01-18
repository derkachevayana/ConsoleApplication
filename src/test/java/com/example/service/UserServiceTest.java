package com.example.service;

import com.example.UserManagementApplication;
import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.exception.UserAlreadyExistsException;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = UserManagementApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_WithWhitespace_ShouldTrimValues() {
        UserRequest request = new UserRequest();
        request.setName("  John Doe  ");
        request.setEmail("  john@example.com  ");
        request.setAge(30);

        UserResponse response = userService.createUser(request);

        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void createUser_DuplicateEmailWithWhitespace_ShouldThrowException() {
        UserRequest request1 = new UserRequest();
        request1.setName("First User");
        request1.setEmail("duplicate@example.com");
        request1.setAge(25);

        UserRequest request2 = new UserRequest();
        request2.setName("Second User");
        request2.setEmail("  duplicate@example.com  ");
        request2.setAge(30);

        userService.createUser(request1);

        assertThatThrownBy(() -> userService.createUser(request2))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void getUserByEmail_WithWhitespace_ShouldTrimAndReturnUser() {
        UserRequest request = new UserRequest();
        request.setName("Bob Johnson");
        request.setEmail("bob@example.com");
        request.setAge(35);

        userService.createUser(request);
        UserResponse found = userService.getUserByEmail("  bob@example.com  ");

        assertThat(found.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void updateUser_EmptyEmailAfterTrim_ShouldNotUpdateEmail() {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Original Name");
        createRequest.setEmail("original@example.com");
        createRequest.setAge(25);

        UserResponse created = userService.createUser(createRequest);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("   ");
        updateRequest.setAge(30);

        UserResponse updated = userService.updateUser(created.getId(), updateRequest);

        assertThat(updated.getEmail()).isEqualTo("original@example.com");
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateUser_SameEmail_ShouldNotCheckForDuplicate() {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Original Name");
        createRequest.setEmail("same@example.com");
        createRequest.setAge(25);

        UserResponse created = userService.createUser(createRequest);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("same@example.com");
        updateRequest.setAge(30);

        UserResponse updated = userService.updateUser(created.getId(), updateRequest);

        assertThat(updated.getEmail()).isEqualTo("same@example.com");
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }
    @Test
    void createUser_EmailWithSpaces_ShouldTrimAndValidate() {
        UserRequest request = new UserRequest();
        request.setName("Test User");
        request.setEmail("  test@example.com  ");
        request.setAge(25);

        UserResponse response = userService.createUser(request);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void createUser_InvalidEmailAfterTrim_ShouldThrowException() {
        UserRequest request = new UserRequest();
        request.setName("Test User");
        request.setEmail("  invalid-email  ");
        request.setAge(25);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must contain @ and a dot in domain");
    }

    @Test
    void updateUser_EmailWithSpaces_ShouldTrimAndValidate() {
        UserRequest createRequest = new UserRequest();
        createRequest.setName("Original");
        createRequest.setEmail("original@example.com");
        createRequest.setAge(25);
        UserResponse created = userService.createUser(createRequest);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated");
        updateRequest.setEmail("  updated@example.com  ");
        updateRequest.setAge(30);

        UserResponse updated = userService.updateUser(created.getId(), updateRequest);
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    }
}
