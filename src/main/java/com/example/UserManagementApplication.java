package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);

        System.out.println("=".repeat(50));
        System.out.println("✅ User Management API started successfully!");
        System.out.println("=".repeat(50));
        System.out.println("📚 REST API Documentation:");
        System.out.println("  • Swagger UI:      http://localhost:8081/swagger-ui.html");
        System.out.println("  • OpenAPI JSON:    http://localhost:8081/api-docs");
        System.out.println("=".repeat(50));
        System.out.println("🚀 REST Endpoints:");
        System.out.println("  • Users API:       http://localhost:8081/api/users");
        System.out.println("  • Health Check:    http://localhost:8081/actuator/health");
        System.out.println("=".repeat(50));
    }
}