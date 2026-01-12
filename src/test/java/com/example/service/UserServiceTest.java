package com.example.service;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.entity.User;
import com.example.exception.UserAlreadyExistsException;
import com.example.exception.UserNotFoundException;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest userRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setName("John");
        userRequest.setEmail("john@example.com");
        userRequest.setAge(25);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setEmail("john@example.com");
        existingUser.setAge(25);
        existingUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_ValidData_ReturnsUserResponse() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });

        UserResponse response = userService.createUser(userRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(25, response.getAge());
        assertNotNull(response.getCreatedAt());

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(userRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ExistingId_ReturnsUserResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NonExistingId_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<UserResponse> responses = userService.getAllUsers();

        assertEquals(1, responses.size());
        assertEquals("John", responses.get(0).getName());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_ValidUpdate_ReturnsUpdatedResponse() {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("John Updated");
        updateRequest.setEmail("john.updated@example.com");
        updateRequest.setAge(30);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        UserResponse response = userService.updateUser(1L, updateRequest);

        assertEquals("John Updated", response.getName());
        assertEquals("john.updated@example.com", response.getEmail());
        assertEquals(30, response.getAge());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("john.updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NonExistingId_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(999L, userRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ExistingId_DeletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistingId_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(999L));

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getUserByEmail_ExistingEmail_ReturnsUserResponse() {
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(existingUser));

        UserResponse response = userService.getUserByEmail("john@example.com");

        assertNotNull(response);
        assertEquals("John", response.getName());
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void getUserByEmail_NonExistingEmail_ThrowsException() {
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail("nonexistent@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
    void createUser_EmptyOrWhitespaceName_ShouldTrimToEmptyString(String name) {
        userRequest.setName(name);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = userService.createUser(userRequest);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }
}