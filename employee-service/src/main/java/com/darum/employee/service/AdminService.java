package com.darum.employee.service;

import com.darum.employee.dto.request.CreateEmployeeRequest;
import com.darum.employee.dto.request.UpdateEmployeeStatusRequest;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.exception.EmployeeNotFoundException;
import com.darum.employee.model.Department;
import com.darum.employee.model.Employee;
import com.darum.employee.model.Status;
import com.darum.employee.repositories.EmployeeRepository;
import com.darum.shared.dto.Roles;
import com.darum.shared.dto.response.UserResponse;
import com.darum.shared.exceptions.UnauthorizedException;
import com.darum.shared.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final EmployeeRepository employeeRepository;
    private final WebClient authWebClient;
    private final ModelMapper modelMapper;

    public Mono<EmployeeResponse> createEmployee(String token , CreateEmployeeRequest createEmployeeRequest, ServerHttpRequest request) {
        // Get headers from the incoming request (from gateway)
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        return authWebClient.get()
                .uri("/auth/me")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Authentication failed: " + e.getMessage())))
                .flatMap(adminUser -> {
                    //  Check if admin has permission
                    if (!hasAdminPrivileges(adminUser.getRoles())) {
                        return Mono.error(new UnauthorizedException("Access denied"));
                    }


                    return authWebClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/auth/user/email")
                                    .queryParam("email", createEmployeeRequest.getEmail()) //
                                    .build())
                            .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                            .retrieve()
                            .bodyToMono(UserResponse.class)
                            .onErrorResume(e -> Mono.error(new RuntimeException("User not found with email: " + createEmployeeRequest.getEmail())))
                            .flatMap(targetUser -> {


                    Department department = Department.fromString(createEmployeeRequest.getDepartment())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid department: " + createEmployeeRequest.getDepartment()
                            ));

                    //  Build new employee
                    Employee employee = new Employee();
                    employee.setEmployeeCode(generateEmployeeCode());
                    employee.setUserId(targetUser.getId());
                    employee.setFirstName(targetUser.getFirstName());
                    employee.setLastName(targetUser.getLastName());
                    employee.setEmail(targetUser.getEmail());
                    employee.setDepartment(department);
                    employee.setStatus(Status.ACTIVE);
                    employee.setCreatedAt(LocalDateTime.now());
                    employee.setUpdatedAt(LocalDateTime.now());
                    return employeeRepository.existsByUserId(targetUser.getId().toString())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new RuntimeException("User already exists as an employee"));
                                }
                                return employeeRepository.save(employee)
                                        .map(savedEmployee -> modelMapper.map(savedEmployee, EmployeeResponse.class));
                            });

                });

                });

    }

    private String generateEmployeeCode() {
        return "EMP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public Flux<EmployeeResponse> getAllEmployees(String token, ServerHttpRequest request) {
        // Get headers from the incoming request (from gateway)
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        System.out.println("=== EMPLOYEE SERVICE: Gateway Headers ===");
        System.out.println("=== X-User-Id: " + userId);
        System.out.println("=== X-User-Email: " + userEmail);
        System.out.println("=== X-User-Roles: " + userRoles);
        return authWebClient.get()
                .uri("/auth/me")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Authentication failed: " + e.getMessage())))
                .flatMapMany(adminUser -> {
                    //  Check if admin has permission
                    if (!hasAdminPrivileges(adminUser.getRoles())) {
                        return Mono.error(new UnauthorizedException("Access denied"));
                    }
                    return employeeRepository.findAll()
                            .map(employee -> modelMapper.map(employee, EmployeeResponse.class));

    });
                }


    @Transactional
    public Mono<EmployeeResponse> updateEmployeeStatus(String token, UpdateEmployeeStatusRequest updateRequest, ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        log.info("🔍 Updating employee status: {} to {}, requested by: {}",
                updateRequest.getEmail(), updateRequest.getStatus(), userEmail);

        // Convert string status to enum
        Status status = convertToStatus(updateRequest.getStatus());
        if (status == null) {
            return Mono.error(new IllegalArgumentException("Invalid status: " + updateRequest.getStatus() +
                    ". Valid statuses: " + Arrays.toString(Status.values())));
        }

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
                    if (!hasAdminPrivileges(currentUser.getRoles())) {
                        return Mono.error(new UnauthorizedException("Access denied: Admin"));
                    }

                    // ✅ NEW: Check if current user can update the target user
                    return checkUpdatePermissions(currentUser, updateRequest.getEmail(), token)
                            .flatMap(canUpdate -> {
                                if (!canUpdate) {
                                    return Mono.error(new UnauthorizedException(
                                            "Access denied: You cannot update status of other administrators or yourself"));
                                }
                                return processEmployeeStatusUpdate(updateRequest.getEmail(), status);
                            });
                });
    }

    // ✅ NEW: Permission Check Method
    private Mono<Boolean> checkUpdatePermissions(UserResponse currentUser, String targetEmail, String token) {
        // SuperAdmin can update anyone (including other SuperAdmins)
        if (currentUser.getRoles().contains(Roles.SUPERADMIN)) {
            return Mono.just(true);
        }

        // Admin cannot update themselves
        if (currentUser.getEmail().equals(targetEmail)) {
            return Mono.just(false);
        }

        // Admin can only update non-admin users
        return authWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/user/email")
                        .queryParam("email", targetEmail)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .map(targetUser -> {
                    // Admin cannot update other Admins or SuperAdmins
                    boolean targetIsAdmin = targetUser.getRoles().contains(Roles.ADMIN) ||
                            targetUser.getRoles().contains(Roles.SUPERADMIN);
                    return !targetIsAdmin;
                })
                .onErrorReturn(false); // If we can't check, deny access
    }


    private Status convertToStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("❌ Invalid status provided: {}", status);
            return null;
        }
    }

    private Mono<EmployeeResponse> processEmployeeStatusUpdate(String email, Status status) {
        return employeeRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException("Employee not found with email: " + email)))
                .flatMap(employee -> {
                    Status oldStatus = employee.getStatus();
                    employee.setStatus(status);
                    employee.setUpdatedAt(LocalDateTime.now());

                    log.info("🔄 Updating employee {} status from {} to {}",
                            employee.getEmail(), oldStatus, status);

                    return employeeRepository.save(employee)
                            .doOnSuccess(updated -> log.info("✅ Successfully updated employee {} status to {}",
                                    updated.getEmail(), updated.getStatus()));
                })
                .map(updatedEmployee -> modelMapper.map(updatedEmployee, EmployeeResponse.class));
    }

    private boolean hasAdminPrivileges(java.util.List<String> roles) {
        return roles.contains(Roles.ADMIN) || roles.contains(Roles.SUPERADMIN);
    }



}
