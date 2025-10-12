package com.darum.employee.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    private UUID employeeId;  // Internal employee-service ID
    @Column("employee_code")
    private String employeeCode; // e.g. EMP-001 (generated internally)
    @Column("user_id")
    private Long userId; // reference to the user in auth-service
    @Column("firstname")
    private String firstName;
    @Column("lastname")
    private String lastName;
    @Column("email")
    private String email;
    @Column("status")
    private Status status; // ACTIVE, INACTIVE, SUSPENDED, etc.
    private List<String> departments;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
