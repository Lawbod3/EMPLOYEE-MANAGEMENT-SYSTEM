package com.darum.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeStatusRequest {
    @NotNull(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotNull(message = "Status is required")
    private String status;
}
