package com.darum.employee.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EmployeeResponse {
    @NotBlank
    private String employeeCode;
    @NotBlank
    private Long userId;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String email;
    @NotBlank
    private String status;
    @NotBlank
    private String department;
    @NotBlank
    private LocalDateTime createdAt;
    @NotBlank
    private LocalDateTime updatedAt;
    @NotBlank
    private List<String> roles;


}
