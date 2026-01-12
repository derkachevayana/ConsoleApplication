package com.example;

import com.example.dto.UserRequest;
import com.example.dto.UserResponse;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class ConsoleRunner implements CommandLineRunner {

    private final UserService userService;
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) {
        System.out.println("=== User Management Application ===");

        boolean mainRunning = true;
        while (mainRunning) {
            System.out.println("\nChoose mode:");
            System.out.println("1. Console mode");
            System.out.println("2. REST API mode (run on port 8081)");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    runConsoleMode();
                    break;
                case "2":
                    System.out.println("REST API running on http://localhost:8081/api/users");
                    return;
                case "3":
                    System.out.println("Exiting application...");
                    mainRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private void runConsoleMode() {
        System.out.println("\n=== Console Mode ===");

        boolean consoleRunning = true;

        while (consoleRunning) {
            printMenu();
            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        createUser();
                        break;
                    case "2":
                        getUserById();
                        break;
                    case "3":
                        getAllUsers();
                        break;
                    case "4":
                        getUserByEmail();
                        break;
                    case "5":
                        updateUser();
                        break;
                    case "6":
                        deleteUser();
                        break;
                    case "0":
                        consoleRunning = false;
                        System.out.println("Returning to main menu...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== MENU ===");
        System.out.println("1. Create user");
        System.out.println("2. Get user by ID");
        System.out.println("3. Get all users");
        System.out.println("4. Get user by email");
        System.out.println("5. Update user");
        System.out.println("6. Delete user");
        System.out.println("0. Back to main menu");
        System.out.print("Enter your choice: ");
    }

    private void createUser() {
        System.out.println("\n--- Create User ---");

        String name;
        do {
            System.out.print("Enter name (required, min 2 chars): ");
            name = scanner.nextLine().trim();
            if (name.length() < 2) {
                System.out.println("Name must be at least 2 characters!");
            }
        } while (name.length() < 2);

        String email;
        do {
            System.out.print("Enter email (required, valid format): ");
            email = scanner.nextLine().trim();
            if (!email.contains("@") || !email.contains(".")) {
                System.out.println("Invalid email format!");
            }
        } while (!email.contains("@") || !email.contains("."));

        Integer age = null;
        while (age == null) {
            System.out.print("Enter age (0-120, required): ");
            String ageInput = scanner.nextLine().trim();
            try {
                age = Integer.parseInt(ageInput);
                if (age < 0 || age > 120) {
                    System.out.println("Age must be between 0 and 120!");
                    age = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }

        try {
            UserRequest request = new UserRequest();
            request.setName(name);
            request.setEmail(email);
            request.setAge(age);

            UserResponse response = userService.createUser(request);
            System.out.println("✓ User created successfully!");
            System.out.println("  ID: " + response.getId());
            System.out.println("  Name: " + response.getName());
            System.out.println("  Email: " + response.getEmail());
            System.out.println("  Created at: " + response.getCreatedAt());
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
    }

    private void getUserById() {
        System.out.println("\n--- Get User by ID ---");

        System.out.print("Enter user ID: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());
            UserResponse response = userService.getUserById(id);
            printUser(response);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        } catch (Exception e) {
            System.out.println("User not found: " + e.getMessage());
        }
    }

    private void getAllUsers() {
        System.out.println("\n--- All Users ---");

        try {
            List<UserResponse> users = userService.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("Total users: " + users.size());
                for (UserResponse user : users) {
                    System.out.println("------------------------");
                    printUser(user);
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
        }
    }

    private void getUserByEmail() {
        System.out.println("\n--- Get User by Email ---");

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        try {
            UserResponse response = userService.getUserByEmail(email);
            printUser(response);
        } catch (Exception e) {
            System.out.println("User not found: " + e.getMessage());
        }
    }

    private void updateUser() {
        System.out.println("\n--- Update User ---");

        System.out.print("Enter user ID to update: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());

            UserResponse existing = userService.getUserById(id);
            System.out.println("Current user:");
            printUser(existing);

            UserRequest request = new UserRequest();

            System.out.print("Enter new name (press Enter to keep '" + existing.getName() + "'): ");
            String name = scanner.nextLine();
            request.setName(name.isEmpty() ? existing.getName() : name);

            System.out.print("Enter new email (press Enter to keep '" + existing.getEmail() + "'): ");
            String email = scanner.nextLine();
            request.setEmail(email.isEmpty() ? existing.getEmail() : email);

            System.out.print("Enter new age (press Enter to keep " + existing.getAge() + "): ");
            String ageInput = scanner.nextLine();
            request.setAge(ageInput.isEmpty() ? existing.getAge() : Integer.parseInt(ageInput));

            UserResponse updated = userService.updateUser(id, request);
            System.out.println("✓ User updated successfully!");
            printUser(updated);

        } catch (NumberFormatException e) {
            System.out.println("Invalid input format.");
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        System.out.println("\n--- Delete User ---");

        System.out.print("Enter user ID to delete: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());

            UserResponse user = userService.getUserById(id);
            System.out.println("About to delete user:");
            printUser(user);

            System.out.print("Are you sure? (yes/no): ");
            String confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("yes")) {
                userService.deleteUser(id);
                System.out.println("✓ User deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printUser(UserResponse user) {
        System.out.println("ID: " + user.getId());
        System.out.println("Name: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Age: " + user.getAge());
        System.out.println("Created: " + user.getCreatedAt());
    }
}
