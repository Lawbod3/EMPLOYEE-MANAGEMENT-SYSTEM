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
public class RoleChangedEvent {
    private String email;
    private String action; // "PROMOTED" or "DEMOTED"
    private String role;
    private String performedBy;
    private LocalDateTime timestamp;
}
