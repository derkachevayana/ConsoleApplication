package com.example.controller;

import com.example.service.CircuitBreakerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class CircuitTestController {

    private final CircuitBreakerService circuitBreakerService;

    @GetMapping("/circuit/{id}")
    public Object testCircuitBreaker(@PathVariable Long id) {
        return circuitBreakerService.getUserWithCircuitBreaker(id);
    }

    @GetMapping("/health")
    public String health() {
        return "User Service is UP";
    }
}