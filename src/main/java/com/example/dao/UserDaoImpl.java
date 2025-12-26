package com.example.dao;

import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public Long save(User user) throws DaoException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Long id = (Long) session.save(user);
            transaction.commit();
            return id;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DaoException("Ошибка при сохранении пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new DaoException("Ошибка при поиске пользователя по ID: " + id, e);
        }
    }

    @Override
    public List<User> findAll() throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            throw new DaoException("Ошибка при получении всех пользователей", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DaoException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new DaoException("Ошибка при поиске пользователя по email: " + email, e);
        }
    }

    @Override
    public void update(User user) throws DaoException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DaoException("Ошибка при обновлении пользователя", e);
        }
    }

    @Override
    public void delete(Long id) throws DaoException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<?> query = session.createQuery("DELETE FROM User WHERE id = :id");
            query.setParameter("id", id);
            int deletedCount = query.executeUpdate();
            transaction.commit();
            if (deletedCount == 0) {
                logger.warn("Попытка удаления несуществующего пользователя с ID={}", id);
            } else {
                logger.info("Удален пользователь ID={}", id);
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Ошибка при удалении пользователя с ID={}", id, e);
            throw new DaoException("Ошибка при удалении пользователя с ID: " + id, e);
        }
    }
}