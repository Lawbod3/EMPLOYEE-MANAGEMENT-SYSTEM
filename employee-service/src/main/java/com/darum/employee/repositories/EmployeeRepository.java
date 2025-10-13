package com.darum.employee.repositories;

import com.darum.employee.model.Employee;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Repository
public interface EmployeeRepository extends ReactiveCrudRepository<Employee, Long> {
    Mono<Boolean> existsByUserId(String userId);

}
