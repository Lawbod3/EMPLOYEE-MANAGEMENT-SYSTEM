package com.darum.notification.service;

import com.darum.notification.model.Notification;
import com.darum.notification.repositories.NotificationRepository;
import com.darum.shared.event.EmployeeCreatedEvent;
import com.darum.shared.event.EmployeeStatusUpdatedEvent;
import com.darum.shared.event.RoleChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private EmailService emailService;

    @KafkaListener(topics = "employee-created", groupId = "notification-group")
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
        log.info("🎯 Received employee created event: {}", event.getEmail());

        String title = "Welcome to the Company!";
        String message = String.format(
                "Hello %s %s! Your employee account has been created. " +
                        "Your employee code is: %s and you're assigned to %s department.",
                event.getFirstName(), event.getLastName(), event.getEmployeeCode(), event.getDepartment()
        );

        createAndSendNotification(
                "EMPLOYEE_CREATED",
                event.getEmail(),
                title,
                message
        ).subscribe();
    }

    @KafkaListener(topics = "employee-status-updated", groupId = "notification-group")
    public void handleEmployeeStatusUpdated(EmployeeStatusUpdatedEvent event) {
        log.info("🎯 Received employee status updated event: {} -> {}",
                event.getEmail(), event.getNewStatus());

        String title = "Account Status Updated";
        String message = String.format(
                "Your account status has been changed from %s to %s by %s.",
                event.getOldStatus(), event.getNewStatus(), event.getUpdatedBy()
        );

        createAndSendNotification(
                "STATUS_UPDATED",
                event.getEmail(),
                title,
                message
        ).subscribe();
    }

    @KafkaListener(topics = "role-changed", groupId = "notification-group")
    public void handleRoleChanged(RoleChangedEvent event) {
        log.info("🎯 Received role changed event: {} {} to {}",
                event.getEmail(), event.getAction(), event.getRole());

        String title = "Role Update";
        String message = String.format(
                "You have been %s to %s by %s.",
                event.getAction().toLowerCase(), event.getRole(), event.getPerformedBy()
        );

        createAndSendNotification(
                "ROLE_CHANGED",
                event.getEmail(),
                title,
                message
        ).subscribe();
    }

    private Mono<Notification> createAndSendNotification(String type, String recipient, String title, String message) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setRecipientEmail(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification)
                .flatMap(savedNotification ->
                        emailService.sendEmail(recipient, title, message)
                                .doOnSuccess(sent -> {
                                    if (sent) {
                                        savedNotification.setSent(true);
                                        savedNotification.setSentAt(LocalDateTime.now());
                                        notificationRepository.save(savedNotification).subscribe();
                                        log.info("✅ Notification sent to: {}", recipient);
                                    }
                                })
                                .thenReturn(savedNotification)
                )
                .doOnError(error -> log.error("❌ Failed to send notification to {}: {}", recipient, error.getMessage()));
    }

    public Flux<Notification> getUserNotifications(String email) {
        return notificationRepository.findByRecipientEmail(email);
    }

    public Flux<Notification> getPendingNotifications() {
        return notificationRepository.findBySentFalse();
    }


}
