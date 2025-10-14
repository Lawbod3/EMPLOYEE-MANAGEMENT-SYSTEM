package com.darum.employee.dto.response;

import com.darum.employee.model.Department;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EmployeeDepartmentResponse {
    @NotNull
    private String employeeCode;
    @NotNull
    private Long userId;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String email;
    @NotNull
    private String status;
    @NotNull
    private Department department;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private LocalDateTime updatedAt;
}
