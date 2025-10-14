package com.darum.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("notifications")
public class Notification {
    @Id
    private Long id;
    private String type; // EMPLOYEE_CREATED, STATUS_UPDATED, ROLE_CHANGED
    private String recipientEmail;
    private String title;
    private String message;
    private boolean sent = false;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
