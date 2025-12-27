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
    private static SessionFactory sessionFactory;

    static {
        initializeSessionFactory();
    }

    private static void initializeSessionFactory() {
        try {
            StandardServiceRegistryBuilder registryBuilder =
                    new StandardServiceRegistryBuilder()
                            .configure("hibernate.cfg.xml");

            String testUrl = System.getProperty("hibernate.connection.url");
            if (testUrl != null) {
                logger.info("Using test database configuration");
                registryBuilder.applySetting("hibernate.connection.url", testUrl);
                registryBuilder.applySetting("hibernate.connection.username",
                        System.getProperty("hibernate.connection.username"));
                registryBuilder.applySetting("hibernate.connection.password",
                        System.getProperty("hibernate.connection.password"));
                registryBuilder.applySetting("hibernate.hbm2ddl.auto", "create-drop");
            }

            StandardServiceRegistry registry = registryBuilder.build();
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            logger.error("Error initializing SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    public static void reinitialize() {
        shutdown();
        initializeSessionFactory();
    }
}