package com.darum.employee.repositories;

import com.darum.employee.model.Employee;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Repository
public interface EmployeeRepository extends ReactiveCrudRepository<Employee, String> {
    // Find employee by userId (reference from auth-service)
    Mono<Employee> findByUserId(String userId);

    // Find employees by department (since it’s stored as a string list)
    Flux<Employee> findByDepartmentsContaining(String department);

    // Find by status (e.g., ACTIVE, INACTIVE)
    Flux<Employee> findByStatus(String status);

    // Optional: if you make employeeCode unique
    Mono<Employee> findByEmployeeCode(String employeeCode);

}
