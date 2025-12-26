package com.example;

import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.util.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDao userDao = new UserDaoImpl();

    public static void main(String[] args) {

        System.out.println("=== User Service CRUD Application ===");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        createUser();
                        break;
                    case "2":
                        findUserById();
                        break;
                    case "3":
                        findAllUsers();
                        break;
                    case "4":
                        updateUser();
                        break;
                    case "5":
                        deleteUser();
                        break;
                    case "6":
                        findUserByEmail();
                        break;
                    case "0":
                        running = false;
                        System.out.println("Выход из приложения...");
                        break;
                    default:
                        System.out.println("Неверный выбор, попробуйте снова.");
                }
            } catch (DaoException e) {
                System.err.println("Ошибка DAO: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Неожиданная ошибка: " + e.getMessage());
            }
        }

        HibernateUtil.shutdown();
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("6. Найти пользователя по email");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private static void createUser() throws DaoException {
        System.out.println("\n--- Создание пользователя ---");

        System.out.print("Введите имя: ");
        String name = scanner.nextLine();

        System.out.print("Введите email: ");
        String email = scanner.nextLine();

        System.out.print("Введите возраст: ");
        int age = Integer.parseInt(scanner.nextLine());

        User user = new User(name, email, age);
        Long id = userDao.save(user);

        System.out.println("Пользователь создан с ID: " + id);
    }

    private static void findUserById() throws DaoException {
        System.out.println("\n--- Поиск пользователя по ID ---");

        System.out.print("Введите ID: ");
        Long id = Long.parseLong(scanner.nextLine());

        Optional<User> user = userDao.findById(id);
        if (user.isPresent()) {
            System.out.println("Найден пользователь: " + user.get());
        } else {
            System.out.println("Пользователь с ID " + id + " не найден");
        }
    }

    private static void findAllUsers() throws DaoException {
        System.out.println("\n--- Список всех пользователей ---");

        List<User> users = userDao.findAll();
        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены");
        } else {
            users.forEach(System.out::println);
        }
    }

    private static void updateUser() throws DaoException {
        System.out.println("\n--- Обновление пользователя ---");

        System.out.print("Введите ID пользователя для обновления: ");
        Long id = Long.parseLong(scanner.nextLine());

        Optional<User> optionalUser = userDao.findById(id);
        if (!optionalUser.isPresent()) {
            System.out.println("Пользователь с ID " + id + " не найден");
            return;
        }

        User user = optionalUser.get();

        System.out.print("Введите новое имя (текущее: " + user.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) {
            user.setName(name);
        }

        System.out.print("Введите новый email (текущий: " + user.getEmail() + "): ");
        String email = scanner.nextLine();
        if (!email.isEmpty()) {
            user.setEmail(email);
        }

        System.out.print("Введите новый возраст (текущий: " + user.getAge() + "): ");
        String ageInput = scanner.nextLine();
        if (!ageInput.isEmpty()) {
            user.setAge(Integer.parseInt(ageInput));
        }

        userDao.update(user);
        System.out.println("Пользователь обновлен");
    }

    private static void deleteUser() throws DaoException {
        System.out.println("\n--- Удаление пользователя ---");

        System.out.print("Введите ID пользователя для удаления: ");
        Long id = Long.parseLong(scanner.nextLine());

        userDao.delete(id);
        System.out.println("Пользователь с ID " + id + " удален");
    }

    private static void findUserByEmail() throws  DaoException {
        System.out.println("\n--- Поиск пользователя по email ---");

        System.out.print("Введите email: ");
        String email = scanner.nextLine();

        Optional<User> user = userDao.findByEmail(email);
        if (user.isPresent()) {
            System.out.println("Найден пользователь " + user.get());
        } else {
            System.out.println("Пользователь с email " + email + " не найден");
        }
    }
}