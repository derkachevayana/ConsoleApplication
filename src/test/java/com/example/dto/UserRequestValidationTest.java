package com.example.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_ShouldHaveNoViolations() {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAge(30);

        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void nameTooShort_ShouldHaveViolation() {
        UserRequest request = new UserRequest();
        request.setName("J");
        request.setEmail("john@example.com");
        request.setAge(30);

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Name must be between 2 and 100 characters");
    }

    @Test
    void ageNegative_ShouldHaveViolation() {
        UserRequest request = new UserRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setAge(-5);

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Age must be positive or zero");
    }
}
