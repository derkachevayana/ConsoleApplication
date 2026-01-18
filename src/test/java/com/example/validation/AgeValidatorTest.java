package com.example.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AgeValidatorTest {

    private final AgeValidator validator = new AgeValidator();

    @Test
    void validateAge_ValidAge_ReturnsValidResult() {
        AgeValidator.ValidationResult result = validator.validateAge(30, true);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isEqualTo(30);
    }

    @Test
    void validateAge_NullWhenRequired_ReturnsError() {
        AgeValidator.ValidationResult result = validator.validateAge(null, true);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getError()).contains("required");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 121})
    void validateAge_OutOfRange_ReturnsError(int invalidAge) {
        AgeValidator.ValidationResult result = validator.validateAge(invalidAge, true);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getError()).contains("between 0 and 120");
    }

    @Test
    void validateAge_OptionalWhenNotRequired_ReturnsEmpty() {
        AgeValidator.ValidationResult result = validator.validateAge(null, false);
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasValue()).isFalse();
    }
}
