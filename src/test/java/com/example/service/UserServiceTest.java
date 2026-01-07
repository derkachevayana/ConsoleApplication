package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.exception.DaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() throws DaoException {
        User user = new User("John", "john@test.com", 25);
        when(userDao.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(1L);

        User result = userService.createUser("John", "john@test.com", 25);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
        verify(userDao).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() throws DaoException {
        User existing = new User("Existing", "john@test.com", 30);
        when(userDao.findByEmail("john@test.com")).thenReturn(Optional.of(existing));

        DaoException exception = assertThrows(DaoException.class, () -> userService.createUser("John", "john@test.com", 25));

        assertTrue(exception.getMessage().contains("Email уже используется"));
        verify(userDao, never()).save(any());
    }

    @Test
    void createUser_InvalidAge_ThrowsException() throws DaoException {
        when(userDao.findByEmail("john@test.com")).thenReturn(Optional.empty());

        DaoException exception = assertThrows(DaoException.class, () -> userService.createUser("John", "john@test.com", -5));

        assertTrue(exception.getMessage().contains("Некорректный возраст"));
    }

    @Test
    void getUserById_Found() throws DaoException {
        User user = new User("John", "john@test.com", 25);
        user.setId(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getName());
    }

    @Test
    void getUserById_NotFound() throws DaoException {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers() throws DaoException {
        List<User> users = List.of(
                new User("John", "john@test.com", 25),
                new User("Jane", "jane@test.com", 30)
        );
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    @Test
    void updateUser_Success() throws DaoException {
        User existing = new User("Old", "old@test.com", 20);
        existing.setId(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail("new@test.com")).thenReturn(Optional.empty());

        userService.updateUser(1L, "New", "new@test.com", 30);

        verify(userDao).update(argThat(user -> user.getName().equals("New") &&

        user.getEmail().equals("new@test.com") &&
                user.getAge() == 30
        ));
    }

    @Test
    void updateUser_NotFound_ThrowsException() throws DaoException {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        DaoException exception = assertThrows(DaoException.class, () -> userService.updateUser(999L, "New", "new@test.com", 30));

        assertTrue(exception.getMessage().contains("не найден"));
        verify(userDao, never()).update(any());
    }

    @Test
    void deleteUser() throws DaoException {
        userService.deleteUser(1L);

        verify(userDao).delete(1L);
    }
}