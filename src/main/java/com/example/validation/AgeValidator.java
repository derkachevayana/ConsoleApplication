package com.example.validation;

import org.springframework.stereotype.Component;

@Component
public class AgeValidator {

    public ValidationResult validateAge(Integer age, boolean required) {
        if (age == null) {
            return required ?
                    ValidationResult.error("Age is required") :
                    ValidationResult.empty();
        }

        if (age < 0 || age > 120) {
            return ValidationResult.error("Age must be between 0 and 120");
        }

        return ValidationResult.valid(age);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final Integer value;
        private final String error;

        private ValidationResult(boolean valid, Integer value, String error) {
            this.valid = valid;
            this.value = value;
            this.error = error;
        }

        public static ValidationResult valid(Integer value) {
            return new ValidationResult(true, value, null);
        }

        public static ValidationResult error(String error) {
            return new ValidationResult(false, null, error);
        }

        public static ValidationResult empty() {
            return new ValidationResult(true, null, null);
        }

        public boolean isValid() { return valid; }
        public Integer getValue() { return value; }
        public String getError() { return error; }
        public boolean hasValue() { return value != null; }
    }
}