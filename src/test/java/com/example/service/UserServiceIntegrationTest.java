package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.dto.UserUpdateRequest;
import com.example.entity.User;
import com.example.exception.UserNotFoundException;
import com.example.kafka.UserEventProducer;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createUser_Success() {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAge(30);

        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        verify(userEventProducer).sendUserEvent(any(), eq("john@example.com"), eq(1L), eq("John Doe"));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    void updateUser_Success() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setAge(35);

        User updatedUser = User.builder()
                .id(1L)
                .name("Updated Name")
                .email("john@example.com")
                .age(35)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse response = userService.updateUser(1L, updateRequest);

        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getAge()).isEqualTo(35);
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(testUser);
        verify(userEventProducer).sendUserEvent(any(), eq("john@example.com"), eq(1L), eq("John Doe"));
    }
}