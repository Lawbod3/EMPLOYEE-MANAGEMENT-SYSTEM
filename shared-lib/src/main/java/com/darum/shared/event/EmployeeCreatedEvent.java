package com.darum.shared.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreatedEvent {
    private String employeeCode;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
    private LocalDateTime createdAt;
}
