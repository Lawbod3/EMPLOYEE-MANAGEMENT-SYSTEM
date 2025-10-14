package com.darum.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromoteToManagerRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotNull(message = "Department Name is required")
    private String department;
}
