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
public class EmployeeStatusUpdatedEvent {
    private String employeeCode;
    private String email;
    private String oldStatus;
    private String newStatus;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
