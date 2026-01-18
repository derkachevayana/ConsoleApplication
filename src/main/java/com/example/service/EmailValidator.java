package com.example.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EmailValidator {

    private static final String EMAIL_REGEX = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$";

    public boolean isValid(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        String trimmedEmail = email.trim();
        return trimmedEmail.matches(EMAIL_REGEX);
    }

    public String validateAndNormalize(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim();

        if (!normalizedEmail.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException(
                    "Email must contain @ and a dot in domain (e.g., user@example.com)"
            );
        }

        return normalizedEmail;
    }

    public String normalizeIfProvided(String email) {
        return email != null ? email.trim() : null;
    }

    public ValidationResult validateEmail(String email, boolean required) {
        if (email == null) {
            return required ?
                    ValidationResult.error("Email is required") :
                    ValidationResult.empty();
        }

        String normalizedEmail = email.trim();

        if (normalizedEmail.isEmpty()) {
            return required ?
                    ValidationResult.error("Email is required") :
                    ValidationResult.empty();
        }

        if (!normalizedEmail.matches(EMAIL_REGEX)) {
            return ValidationResult.error(
                    "Email must contain @ and a dot in domain (e.g., user@example.com)"
            );
        }

        return ValidationResult.valid(normalizedEmail);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String value;
        private final String error;

        private ValidationResult(boolean valid, String value, String error) {
            this.valid = valid;
            this.value = value;
            this.error = error;
        }

        public static ValidationResult valid(String value) {
            return new ValidationResult(true, value, null);
        }

        public static ValidationResult error(String error) {
            return new ValidationResult(false, null, error);
        }

        public static ValidationResult empty() {
            return new ValidationResult(true, null, null);
        }

        public boolean isValid() { return valid; }
        public String getValue() { return value; }
        public String getError() { return error; }
        public boolean hasValue() { return value != null && !value.isEmpty(); }
    }
}
