package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.exception.DaoException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceParameterizedTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @ValueSource(strings = {"test@example.com", "user@mail.ru", "admin@gmail.com"})
    void getUserByEmail_WithDifferentEmails(String email) throws DaoException {
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByEmail(email);

        assertTrue(result.isEmpty());
        verify(userDao).findByEmail(email);
    }

    @ParameterizedTest
    @CsvSource({
            "John, john@test.com, 25",
            "Alice, alice@mail.com, 30",
            "Bob, bob@gmail.com, 40",
            "Eve, eve@example.org, 18"
    })
    void createUser_WithDifferentValidData(String name, String email, int age) throws DaoException {
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(1L);

        User user = userService.createUser(name, email, age);

        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(age, user.getAge());
        verify(userDao).save(any(User.class));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/invalid-user-data.csv", numLinesToSkip = 1)
    void createUser_WithInvalidAge_ShouldThrowException(String name, String email, int age) throws DaoException {
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        DaoException exception = assertThrows(DaoException.class, () -> userService.createUser(name, email, age));

        assertTrue(exception.getMessage().contains("Некорректный возраст"));
        verify(userDao, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void updateUser_WithEmptyOrNullName_ShouldNotUpdateName(String name) throws DaoException {
        User existingUser = new User("Original", "test@example.com", 25);
        existingUser.setId(1L);

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, name, null, 30);

        assertEquals("Original", existingUser.getName());
        assertEquals(30, existingUser.getAge());
    }

    @ParameterizedTest
    @CsvSource({
            "'  John  ', '  john@test.com  '",
            "'\tAlice\n', '\talice@test.com\n'",
            "'Bob  Smith', 'bob.smith@test.com'",
            "'  ', 'test@example.com'"
    })
    void createUser_WithSpaces_ShouldNotThrowException(String name, String email) throws DaoException {
        String trimmedEmail = email.trim();
        when(userDao.findByEmail(trimmedEmail)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(1L);

        assertDoesNotThrow(() -> userService.createUser(name, email, 25));

        verify(userDao).findByEmail(trimmedEmail);
        verify(userDao).save(any(User.class));
    }

    @ParameterizedTest
    @CsvSource({
            "'  John  '",
            "'\tAlice\n'",
            "'  Bob  Smith  '",
            "'Normal'",
            "'  '",
            "''"
    })
    void updateUser_ShouldTrimName(String name) throws DaoException {
        User existingUser = new User("OldName", "old@example.com", 20);
        existingUser.setId(1L);

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        assertDoesNotThrow(() ->
                userService.updateUser(1L, name, null, 30)
        );

        verify(userDao).findById(1L);
        verify(userDao).update(any(User.class));
    }

    @ParameterizedTest
    @CsvSource({
            "'  new@test.com  '",
            "'\temail@test.com\n'",
            "'normal@test.com'"
    })
    void updateUser_ShouldTrimEmail(String email) throws DaoException {
        User existingUser = new User("John", "old@example.com", 20);
        existingUser.setId(1L);

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail(email.trim())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.updateUser(1L, null, email, 30));

        verify(userDao).findByEmail(email.trim());
        verify(userDao).update(any(User.class));
    }


}