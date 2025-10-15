package com.darum.employee.publisher;

import com.darum.employee.model.Employee;
import com.darum.shared.event.EmployeeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotification {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Void> publishEmployeeCreatedEvent(Employee employee, String createdByAdmin) {
        return Mono.fromRunnable(() -> {
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                    employee.getEmployeeCode(),
                    employee.getEmail(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getDepartment().name(),
                    employee.getCreatedAt()
            );

            kafkaTemplate.send("employee-created", event);
            log.info("✅ Published employee created event for: {} | Created by: {}",
                    employee.getEmail(), createdByAdmin);
        });
    }
}
