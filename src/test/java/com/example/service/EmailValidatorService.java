package com.example.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailValidatorTest {

    private final EmailValidator emailValidator = new EmailValidator();

    @Test
    void isValid_ValidEmail_ReturnsTrue() {
        assertThat(emailValidator.isValid("test@example.com")).isTrue();
        assertThat(emailValidator.isValid("user.name@domain.co.uk")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "missing@dot", "@domain.com", "user@.com"})
    void isValid_InvalidEmail_ReturnsFalse(String invalidEmail) {
        assertThat(emailValidator.isValid(invalidEmail)).isFalse();
    }

    @Test
    void isValid_NullOrEmpty_ReturnsFalse() {
        assertThat(emailValidator.isValid(null)).isFalse();
        assertThat(emailValidator.isValid("")).isFalse();
        assertThat(emailValidator.isValid("   ")).isFalse();
    }

    @Test
    void validateAndNormalize_ValidEmail_ReturnsTrimmedEmail() {
        String result = emailValidator.validateAndNormalize("  test@example.com  ");
        assertThat(result).isEqualTo("test@example.com");
    }

    @Test
    void validateAndNormalize_InvalidEmail_ThrowsException() {
        assertThatThrownBy(() -> emailValidator.validateAndNormalize("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email must contain @");
    }
}
