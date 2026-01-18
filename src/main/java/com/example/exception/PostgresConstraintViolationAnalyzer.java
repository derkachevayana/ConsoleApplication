package com.example.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class PostgresConstraintViolationAnalyzer implements ConstraintViolationAnalyzer {

    @Override
    public boolean isEmailConstraintViolation(DataIntegrityViolationException ex) {
        return analyzeViolation(ex, "").isEmailViolation();
    }

    @Override
    public RuntimeException convertToBusinessException(DataIntegrityViolationException ex, String email) {
        ViolationAnalysis analysis = analyzeViolation(ex, email);
        if (analysis.isEmailViolation()) {
            return new UserAlreadyExistsException(analysis.getMessage(), ex);
        }
        return ex;
    }

    private ViolationAnalysis analyzeViolation(DataIntegrityViolationException ex, String email) {
        Throwable rootCause = ex.getRootCause();
        String message = ex.getMessage();

        if (rootCause instanceof SQLException) {
            SQLException sqlEx = (SQLException) rootCause;
            String sqlState = sqlEx.getSQLState();

            if ("23505".equals(sqlState) || "23000".equals(sqlState)) {
                return ViolationAnalysis.emailViolation("Email already exists: " + email);
            }
        }

        if (ex.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) ex.getCause();
            String constraintName = cve.getConstraintName();

            if (constraintName != null && constraintName.toLowerCase().contains("email")) {
                return ViolationAnalysis.emailViolation("Email already exists: " + email);
            }
        }

        if (message != null && (
                message.toLowerCase().contains("email") ||
                        message.contains("23505") ||
                        message.toLowerCase().contains("duplicate") ||
                        message.toLowerCase().contains("unique")
        )) {
            return ViolationAnalysis.emailViolation("Email already exists: " + email);
        }

        return ViolationAnalysis.notEmailViolation();
    }

    private static class ViolationAnalysis {
        private final boolean emailViolation;
        private final String message;

        static ViolationAnalysis emailViolation(String message) {
            return new ViolationAnalysis(true, message);
        }

        static ViolationAnalysis notEmailViolation() {
            return new ViolationAnalysis(false, null);
        }

        private ViolationAnalysis(boolean emailViolation, String message) {
            this.emailViolation = emailViolation;
            this.message = message;
        }

        boolean isEmailViolation() { return emailViolation; }
        String getMessage() { return message; }
    }
}
