package com.example.util;

import com.example.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml")
                    .build();

            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .getMetadataBuilder()
                    .build();
            logger.info("SessionFactory успешно создана");
            return metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            logger.error("Ошибка инициализации SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public static void shutdown() {
        try {
            getSessionFactory().close();
            logger.info("SessionFactory закрыта");
        } catch (Exception e) {
            logger.error("Ошибка при закрытии SessionFactory", e);
        }
    }
}
