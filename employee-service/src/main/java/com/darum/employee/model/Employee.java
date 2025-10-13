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
    @Column("employee_id")  // This is your primary key in DB
    private Long id;  // Maps to employee_id BIGSERIAL

    @Column("employee_code")
    private String employeeCode;

    @Column("user_id")
    private Long userId;

    @Column("first_name")  // Match database column name
    private String firstName;

    @Column("last_name")   // Match database column name
    private String lastName;

    @Column("email")
    private String email;

    @Column("status")
    private Status status;

    @Column("departments")  // Match database column name
    private Department department;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

}
