package com.darum.notification.repositories;

import com.darum.notification.model.Notification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRepository extends R2dbcRepository<Notification, Long> {
    Flux<Notification> findByRecipientEmail(String email);

    Flux<Notification> findBySentFalse();
}
