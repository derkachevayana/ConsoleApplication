package com.example.exception;

import org.springframework.dao.DataIntegrityViolationException;

public interface ConstraintViolationAnalyzer {
    boolean isEmailConstraintViolation(DataIntegrityViolationException ex);
    RuntimeException convertToBusinessException(DataIntegrityViolationException ex, String email);
}
