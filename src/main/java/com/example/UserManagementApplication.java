package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
        System.out.println("=".repeat(50));
        System.out.println("✅ User Management API started successfully!");
        System.out.println("=".repeat(50));
        System.out.println("📡 Service Registry: Eureka");
        System.out.println("⚡ Circuit Breaker: Enabled (Resilience4j)");
        System.out.println("🔧 Config Server: Connected");
        System.out.println("=".repeat(50));
        System.out.println("📚 REST API Documentation:");
        System.out.println("  • Gateway Swagger UI: http://localhost:8090/swagger-ui.html");
        System.out.println("  • Direct Swagger UI:  http://localhost:8081/swagger-ui.html");
        System.out.println("  • OpenAPI JSON:       http://localhost:8090/api-docs");
        System.out.println("=".repeat(50));
        System.out.println("🚀 REST Endpoints:");
        System.out.println("  • Direct Access:      http://localhost:8081/api/users");
        System.out.println("  • Via Gateway:        http://localhost:8090/api/users");
        System.out.println("  • Health Check:       http://localhost:8081/actuator/health");
        System.out.println("  • Circuit Breaker:    http://localhost:8081/actuator/circuitbreakers");
        System.out.println("=".repeat(50));
    }
}