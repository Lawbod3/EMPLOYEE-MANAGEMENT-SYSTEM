package com.darum.employee.service;

import com.darum.employee.dto.request.UpdateDepartmentRequest;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.exception.EmployeeNotFoundException;
import com.darum.employee.model.Department;
import com.darum.employee.repositories.EmployeeRepository;
import com.darum.shared.dto.Roles;
import com.darum.shared.dto.response.UserResponse;
import com.darum.shared.exceptions.UnauthorizedException;
import com.darum.shared.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final EmployeeRepository employeeRepository;
    private final WebClient authWebClient;
    private final ModelMapper modelMapper;

    public Flux<Department> getAllDepartments(String token, ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        log.info("🔍 Fetching all departments, requested by: {}", userEmail);

        return authWebClient.get()
                .uri("/auth/me")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Authentication failed: " + e.getMessage())))
                .flatMapMany(currentUser -> {
                    // Anyone who is authenticated can view departments
                    return Flux.fromArray(Department.values());
                });
    }



    // Add this method to your existing EmployeeService class

    @Transactional
    public Mono<EmployeeResponse> updateEmployeeDepartment(String token, UpdateDepartmentRequest updateRequest, ServerHttpRequest serverRequest) {
        String userId = serverRequest.getHeaders().getFirst("X-User-Id");
        String userEmail = serverRequest.getHeaders().getFirst("X-User-Email");
        String userRoles = serverRequest.getHeaders().getFirst("X-User-Roles");

        log.info("🔍 Updating employee department - Employee: {}, New Department: {}, requested by: {}",
                updateRequest.getEmployeeCode(), updateRequest.getDepartment(), userEmail);

        return authWebClient.get()
                .uri("/auth/me")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Authentication failed: " + e.getMessage())))
                .flatMap(currentUser -> {
                    // Only Admin and SuperAdmin can update departments
                    if (!hasDepartmentUpdatePrivileges(currentUser.getRoles())) {
                        return Mono.error(new UnauthorizedException(
                                "Access denied"));
                    }

                    // Convert string department to enum
                    Department newDepartment = convertToDepartment(updateRequest.getDepartment());
                    if (newDepartment == null) {
                        return Mono.error(new IllegalArgumentException("Invalid department: " + updateRequest.getDepartment() +
                                ". Valid departments: " + Arrays.toString(Department.values())));
                    }

                    return employeeRepository.findByEmployeeCode(updateRequest.getEmployeeCode())
                            .switchIfEmpty(Mono.error(new EmployeeNotFoundException("Employee not found with code: " + updateRequest.getEmployeeCode())))
                            .flatMap(employee -> {
                                Department oldDepartment = employee.getDepartment();
                                employee.setDepartment(newDepartment);
                                employee.setUpdatedAt(LocalDateTime.now());

                                log.info("🔄 Updating employee {} department from {} to {}",
                                        employee.getEmail(), oldDepartment, newDepartment);

                                return employeeRepository.save(employee)
                                        .doOnSuccess(updated -> log.info("✅ Successfully updated employee {} department to {}",
                                                updated.getEmail(), updated.getDepartment()));
                            })
                            .map(updatedEmployee -> modelMapper.map(updatedEmployee, EmployeeResponse.class));
                });
    }

    private Department convertToDepartment(String department) {
        try {
            return Department.valueOf(department.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("❌ Invalid department provided: {}", department);
            return null;
        }
    }

    private boolean hasDepartmentUpdatePrivileges(java.util.List<String> roles) {
        return roles.contains(Roles.ADMIN) || roles.contains(Roles.SUPERADMIN);
    }

}
