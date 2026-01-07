package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.exception.DaoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceCaptorTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<Long> longCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Test
    void createUser_ShouldPassCorrectDataToDao() throws DaoException {
        when(userDao.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userDao.save(userCaptor.capture())).thenReturn(1L);

        User result = userService.createUser("John", "test@example.com", 25);

        User capturedUser = userCaptor.getValue();
        assertEquals("John", capturedUser.getName());
        assertEquals("test@example.com", capturedUser.getEmail());
        assertEquals(25, capturedUser.getAge());

        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
    }

    @Test
    void createUser_ShouldCaptureUserWithInvalidAge() throws DaoException {
        when(userDao.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(DaoException.class, () -> userService.createUser("John", "test@example.com", -5));

        verify(userDao, never()).save(any());
    }

    @Test
    void updateUser_ShouldCaptureUpdatedUser() throws DaoException {
        User existingUser = new User("OldName", "old@example.com", 20);
        existingUser.setId(1L);

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("new@example.com")).thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        doNothing().when(userDao).update(userCaptor.capture());

        userService.updateUser(1L, "NewName", "new@example.com", 30);

        verify(userDao).update(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals(1L, updatedUser.getId());
        assertEquals("NewName", updatedUser.getName());
        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());
    }

    @Test
    void updateUser_ShouldNotUpdateIfEmailExists() throws DaoException {
        User existing = new User("John", "john@example.com", 25);
        existing.setId(1L);

        User otherUser = new User("Other", "other@example.com", 30);

        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(DaoException.class, () ->
                userService.updateUser(1L, "NewName", "other@example.com", 30)
        );

        verify(userDao, never()).update(any());
    }

    @Test
    void deleteUser_ShouldCaptureCorrectId() throws DaoException {
        userService.deleteUser(123L);

        verify(userDao).delete(longCaptor.capture());
        Long capturedId = longCaptor.getValue();

        assertEquals(123L, capturedId);
    }

    @Test
    void getUserByEmail_ShouldCaptureEmail() throws DaoException {
        String email = "test@example.com";
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        userService.getUserByEmail(email);

        verify(userDao).findByEmail(stringCaptor.capture());
        String capturedEmail = stringCaptor.getValue();

        assertEquals(email, capturedEmail);
    }

    @ParameterizedTest
    @CsvSource({
            "' John ', 'John', 'test@example.com'",
            "'\tAlice\n', 'Alice', 'test@example.com'",
            "'Bob', 'Bob', ' test@example.com '",
            "' Bob ', 'Bob', '\ttest@example.com\n'"
    })
    void updateUser_ShouldTrimNameAndEmail(String inputName, String expectedName, String inputEmail) throws DaoException {
        User existing = new User("Old", "old@example.com", 20);
        existing.setId(1L);

        String expectedEmail = inputEmail.trim();
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail(expectedEmail)).thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        doNothing().when(userDao).update(userCaptor.capture());

        userService.updateUser(1L, inputName, inputEmail, 30);

        User capturedUser = userCaptor.getValue();
        assertEquals(expectedName, capturedUser.getName());
        assertEquals(expectedEmail, capturedUser.getEmail());
        assertEquals(30, capturedUser.getAge());
    }

    @Test
    void createUser_ShouldTrimSpaces() throws DaoException {
        when(userDao.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userDao.save(userCaptor.capture())).thenReturn(1L);

        User result = userService.createUser(" John ", " test@example.com", 25);

        User capturedUser = userCaptor.getValue();
        assertEquals("John", capturedUser.getName());
        assertEquals("test@example.com", capturedUser.getEmail());
    }
}
