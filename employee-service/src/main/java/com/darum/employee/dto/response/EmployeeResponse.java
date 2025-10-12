package com.darum.employee.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EmployeeResponse {
    private String employeeId;
    private String employeeCode;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private List<String> departments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
