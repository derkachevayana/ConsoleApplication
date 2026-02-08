package com.example.service;

import com.example.dto.UserResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class CircuitBreakerService {

    private static final String USER_SERVICE_CB = "userService";
    private final RestTemplate restTemplate = new RestTemplate();

    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "fallbackGetUser")
    @Retry(name = USER_SERVICE_CB, fallbackMethod = "fallbackGetUser")
    public UserResponse getUserWithCircuitBreaker(Long id) {
        log.info("Calling user service with Circuit Breaker protection for user id: {}", id);
        simulateExternalCall();

        return UserResponse.builder()
                .id(id)
                .name("External Service User")
                .email("user" + id + "@external.com")
                .age(25 + (int)(Math.random() * 20))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UserResponse fallbackGetUser(Long id, Throwable t) {
        log.warn("⚠️ Circuit Breaker fallback triggered for user id: {}. Error: {}", id, t.getMessage());

        return UserResponse.builder()
                .id(id)
                .name("Fallback User (Service Unavailable)")
                .email("fallback@example.com")
                .age(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @CircuitBreaker(name = USER_SERVICE_CB, fallbackMethod = "fallbackEmailCheck")
    public boolean checkEmailAvailability(String email) {
        log.info("Checking email availability with Circuit Breaker: {}", email);
        if (Math.random() > 0.7) {
            throw new RuntimeException("Email service timeout");
        }
        return true;
    }

    public boolean fallbackEmailCheck(String email, Throwable t) {
        log.warn("Email check fallback for: {}. Error: {}", email, t.getMessage());
        return false;
    }

    private void simulateExternalCall() {
        if (Math.random() > 0.6) {
            throw new RuntimeException("External service unavailable");
        }

        try {
            Thread.sleep(50 + (long)(Math.random() * 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
