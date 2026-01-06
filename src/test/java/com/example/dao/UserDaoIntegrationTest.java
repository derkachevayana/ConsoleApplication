package com.example.dao;

import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.testutil.DatabaseCleaner;
import com.example.util.HibernateUtil;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static UserDaoImpl userDao;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());

        HibernateUtil.reinitialize();
        userDao = new UserDaoImpl();
    }

    @BeforeEach
    void setUp() {
        DatabaseCleaner.clearUsers();
    }

    @AfterAll
    static void afterAll() {
        HibernateUtil.shutdown();
    }

    @Test
    void saveUser_ShouldReturnId() throws DaoException {
        String uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
        User user = new User("Test User", uniqueEmail, 25);

        Long id = userDao.save(user);

        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    void findById_ShouldReturnSavedUser() throws DaoException {
        String uniqueEmail = "find-" + UUID.randomUUID() + "@example.com";
        User user = new User("Find User", uniqueEmail, 30);
        Long id = userDao.save(user);

        Optional<User> found = userDao.findById(id);

        assertTrue(found.isPresent());
        assertEquals("Find User", found.get().getName());
        assertEquals(uniqueEmail, found.get().getEmail());
        assertEquals(30, found.get().getAge());
    }

    @Test
    void findByEmail_ShouldReturnUser() throws DaoException {
        String uniqueEmail = "email-" + UUID.randomUUID() + "@example.com";
        User user = new User("Email User", uniqueEmail, 35);
        userDao.save(user);

        Optional<User> found = userDao.findByEmail(uniqueEmail);

        assertTrue(found.isPresent());
        assertEquals("Email User", found.get().getName());
    }

    @Test
    void findAll_ShouldReturnMultipleUsers() throws DaoException {
        userDao.save(new User("User1", "user1-" + UUID.randomUUID() + "@example.com", 20));
        userDao.save(new User("User2", "user2-" + UUID.randomUUID() + "@example.com", 25));

        List<User> users = userDao.findAll();

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    void findAll_EmptyDatabase_ShouldReturnEmptyList() throws DaoException {
        List<User> users = userDao.findAll();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void updateUser_ShouldModifyData() throws DaoException {
        String uniqueEmail = "update-" + UUID.randomUUID() + "@example.com";
        User user = new User("Before Update", uniqueEmail, 40);
        Long id = userDao.save(user);

        User savedUser = userDao.findById(id).orElseThrow(() -> new AssertionError("Пользователь должен существовать"));

        savedUser.setName("After Update");
        savedUser.setAge(45);

        userDao.update(savedUser);

        Optional<User> updated = userDao.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("After Update", updated.get().getName());
        assertEquals(45, updated.get().getAge());
    }

    @Test
    void deleteUser_ShouldRemoveFromDatabase() throws DaoException {
        String uniqueEmail = "delete-" + UUID.randomUUID() + "@example.com";
        User user = new User("To Delete", uniqueEmail, 50);
        Long id = userDao.save(user);

        userDao.delete(id);

        Optional<User> deleted = userDao.findById(id);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void saveUser_DuplicateEmail_ShouldThrowException() throws DaoException {
        String duplicateEmail = "duplicate-" + UUID.randomUUID() + "@example.com";

        User user1 = new User("First User", duplicateEmail, 25);
        Long id1 = userDao.save(user1);
        assertNotNull(id1);

        User user2 = new User("Second User", duplicateEmail, 30);

        DaoException exception = assertThrows(DaoException.class, () -> userDao.save(user2));

        assertTrue(exception.getMessage().contains("уже используется") ||
                exception.getMessage().contains("Email"));
    }

    @Test
    void atomicDelete_ShouldWork() throws DaoException {
        String email1 = "atomic1-" + UUID.randomUUID() + "@example.com";
        String email2 = "atomic2-" + UUID.randomUUID() + "@example.com";

        Long id1 = userDao.save(new User("Atomic1", email1, 25));
        Long id2 = userDao.save(new User("Atomic2", email2, 30));

        userDao.delete(id1);

        assertTrue(userDao.findById(id1).isEmpty());
        assertTrue(userDao.findById(id2).isPresent());
    }
}