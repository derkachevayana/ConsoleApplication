package com.example.testutil;

import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class DatabaseCleaner {

    public static void clearUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Query<?> deleteQuery = session.createQuery("DELETE FROM User");
            deleteQuery.executeUpdate();

            transaction.commit();
        }
    }
}
