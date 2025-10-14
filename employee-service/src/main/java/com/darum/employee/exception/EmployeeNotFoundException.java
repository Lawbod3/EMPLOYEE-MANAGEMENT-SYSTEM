package com.darum.employee.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
