package com.example.dao;

import com.example.entity.User;
import com.example.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Long save(User user) throws DaoException;

    Optional<User> findById(Long id) throws DaoException;
    List<User> findAll() throws DaoException;
    Optional<User> findByEmail(String email) throws DaoException;

    void update(User user) throws DaoException;

    void delete(Long id) throws DaoException;
}
