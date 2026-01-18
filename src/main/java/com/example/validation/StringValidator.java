package com.example.validation;

import org.springframework.stereotype.Component;

@Component
public class StringValidator {

    public ValidationResult validateName(String name, boolean required) {
        if (name == null) {
            return required ?
                    ValidationResult.error("Name is required") :
                    ValidationResult.empty();
        }

        String normalizedName = name.trim();

        if (normalizedName.isEmpty()) {
            return required ?
                    ValidationResult.error("Name is required") :
                    ValidationResult.empty();
        }

        if (normalizedName.length() < 2 || normalizedName.length() > 100) {
            return ValidationResult.error("Name must be between 2 and 100 characters");
        }

        return ValidationResult.valid(normalizedName);
    }

    public String normalize(String value) {
        return value != null ? value.trim() : null;
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
