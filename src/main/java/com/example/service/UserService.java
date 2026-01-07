package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUser(String name, String email, Integer age) throws DaoException {
        logger.info("Создание пользователя: {}, {}", name, email);

        String trimmedEmail = (email != null) ? email.trim() : email;

        if (userDao.findByEmail(trimmedEmail).isPresent()) {
            throw new DaoException("Email уже используется: " + trimmedEmail);
        }

        if (age != null && (age < 0 || age > 120)) {
            throw new DaoException("Некорректный возраст: " + age);
        }

        String trimmedName = (name != null) ? name.trim() : name;
        User user = new User(trimmedName, trimmedEmail, age);

        Long id = userDao.save(user);
        user.setId(id);
        return user;
    }

    public Optional<User> getUserById(Long id) throws DaoException {
        return userDao.findById(id);
    }

    public List<User> getAllUsers() throws DaoException {
        return userDao.findAll();
    }

    public Optional<User> getUserByEmail(String email) throws DaoException {
        return userDao.findByEmail(email);
    }

    public void updateUser(Long id, String name, String email, Integer age) throws DaoException {
        User user = userDao.findById(id)
                        .orElseThrow(() -> new DaoException("Пользователь не найден " + id));

        if (email != null) {
            String trimmedEmail = email.trim();
            if (!trimmedEmail.equals(user.getEmail())) {
                if (userDao.findByEmail(trimmedEmail).isPresent()) {
                    throw new DaoException("Пользователь с email " + trimmedEmail + " уже существует");
                }
                user.setEmail(trimmedEmail);
            }
        }

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }

        if (age != null) {
            user.setAge(age);
        }

        userDao.update(user);
    }

    public void deleteUser(Long id) throws DaoException {
        userDao.delete(id);
    }
}
