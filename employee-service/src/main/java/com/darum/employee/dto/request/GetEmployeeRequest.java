package com.darum.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetEmployeeRequest {
    @NotBlank(message = "Employee code is required")
    private String employeeCode;
}
