package com.example.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringValidatorTest {

    private final StringValidator validator = new StringValidator();

    @Test
    void validateName_ValidName_ReturnsValidResult() {
        StringValidator.ValidationResult result = validator.validateName("John Doe", true);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isEqualTo("John Doe");
    }

    @Test
    void validateName_NullWhenRequired_ReturnsError() {
        StringValidator.ValidationResult result = validator.validateName(null, true);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getError()).contains("required");
    }

    @Test
    void validateName_EmptyWhenRequired_ReturnsError() {
        StringValidator.ValidationResult result = validator.validateName("   ", true);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getError()).contains("required");
    }

    @Test
    void normalize_ReturnsTrimmedString() {
        assertThat(validator.normalize("  test  ")).isEqualTo("test");
        assertThat(validator.normalize(null)).isNull();
    }
}
