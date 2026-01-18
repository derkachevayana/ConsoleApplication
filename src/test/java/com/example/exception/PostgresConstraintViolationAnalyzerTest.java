package com.example.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgresConstraintViolationAnalyzerTest {

    private final PostgresConstraintViolationAnalyzer analyzer =
            new PostgresConstraintViolationAnalyzer();

    @Test
    void isEmailConstraintViolation_PostgresUniqueViolation_ReturnsTrue() {
        SQLException sqlEx = mock(SQLException.class);
        when(sqlEx.getSQLState()).thenReturn("23505");

        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Violation", sqlEx);

        assertThat(analyzer.isEmailConstraintViolation(ex)).isTrue();
    }

    @Test
    void convertToBusinessException_EmailViolation_ReturnsUserAlreadyExistsException() {
        SQLException sqlEx = mock(SQLException.class);
        when(sqlEx.getSQLState()).thenReturn("23505");

        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Duplicate email", sqlEx);

        RuntimeException result = analyzer.convertToBusinessException(ex, "test@example.com");

        assertThat(result).isInstanceOf(UserAlreadyExistsException.class);
        assertThat(result.getMessage()).contains("test@example.com");
    }

    @Test
    void convertToBusinessException_NonEmailViolation_ReturnsOriginalException() {
        SQLException sqlEx = mock(SQLException.class);
        when(sqlEx.getSQLState()).thenReturn("23503");

        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("FK violation", sqlEx);

        RuntimeException result = analyzer.convertToBusinessException(ex, "test@example.com");

        assertThat(result).isSameAs(ex);
    }
}
