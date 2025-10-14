package com.darum.employee.service;

import com.darum.employee.dto.request.PromoteToManagerRequest;
import com.darum.employee.dto.response.EmployeeDepartmentResponse;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.exception.DepartmentNotFoundException;
import com.darum.employee.exception.EmployeeNotFoundException;
import com.darum.employee.model.Department;
import com.darum.employee.model.Employee;
import com.darum.employee.repositories.EmployeeRepository;
import com.darum.shared.dto.Roles;
import com.darum.shared.dto.request.AddRoleRequest;
import com.darum.shared.dto.response.ApiResponse;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagerService {
    private final WebClient authWebClient;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Mono<EmployeeResponse> promoteToManager(String token, PromoteToManagerRequest promoteRequest, ServerHttpRequest request) {
        // Get headers from gateway
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        log.info("🔍 Promoting user to manager: {}, requested by: {}", promoteRequest.getEmail(), userEmail);

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
                    // Check if user has Admin or SuperAdmin privileges
                    if (!hasAdminPrivileges(adminUser.getRoles())) {
                        return Mono.error(new UnauthorizedException("Access denied: Admin privileges required"));
                    }

                    return processManagerPromotion(promoteRequest, token, request, adminUser);
                });
    }

    private boolean hasAdminPrivileges(java.util.List<String> roles) {
        return roles.contains(Roles.ADMIN) || roles.contains(Roles.SUPERADMIN);
    }

    private Mono<EmployeeResponse> processManagerPromotion(PromoteToManagerRequest promoteRequest,
                                                           String token, ServerHttpRequest request,
                                                           UserResponse adminUser) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        // Convert string department to enum
        Department department = convertToDepartment(promoteRequest.getDepartment());
        if (department == null) {
            return Mono.error(new DepartmentNotFoundException("Invalid department: " + promoteRequest.getDepartment() +
                    ". Valid departments: " + java.util.Arrays.toString(Department.values())));
        }

        return findEmployeeByEmail(promoteRequest.getEmail())
                .flatMap(targetEmployee -> {
                    log.info("🔍 Found employee: {} for department: {}", targetEmployee.getEmail(), department);

                    return authWebClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/auth/user/email")
                                    .queryParam("email", promoteRequest.getEmail())
                                    .build())
                            .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                            .header("X-User-Id", userId)
                            .header("X-User-Email", userEmail)
                            .header("X-User-Roles", userRoles)
                            .retrieve()
                            .bodyToMono(UserResponse.class)
                            .onErrorResume(e -> Mono.error(new EmployeeNotFoundException("User not found with email: " + promoteRequest.getEmail())))
                            .flatMap(targetUser -> {
                                // Add MANAGER role via Auth Service
                                return addManagerRoleToUser(targetUser.getId(), token, request)
                                        .then(updateEmployeeAsManager(targetEmployee, department))
                                        .then(getUpdatedUser(targetUser.getEmail(), token, request)) // ← Get user with new ro
                                        .map(updatedUser -> { // ← Use the UPDATED user here
                                            EmployeeResponse response = modelMapper.map(targetEmployee, EmployeeResponse.class);
                                            response.setRoles(updatedUser.getRoles()); // ← Use the UPDATED user's roles
                                            log.info("✅ Final roles after promotion: {}", updatedUser.getRoles());
                                            return response;
                                        });
                            });
                });
    }
    private Mono<Employee> findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException("Employee not found with email: " + email)));
    }

    private Department convertToDepartment(String department) {
        try {
            return Department.valueOf(department.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("❌ Invalid department provided: {}", department);
            return null;
        }
    }

    private Mono<UserResponse> addManagerRoleToUser(Long userId, String token, ServerHttpRequest request) {
        String requestUserId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        return authWebClient.put()
                .uri("/auth/users/" + userId + "/roles")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", requestUserId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .bodyValue(new AddRoleRequest(Roles.MANAGER))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    return response.bodyToMono(ApiResponse.class)
                            .flatMap(apiResponse -> Mono.error(new RuntimeException(apiResponse.getData().toString())));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new RuntimeException("Auth service error: " + errorBody)));
                })
                .bodyToMono(UserResponse.class)
                .doOnSuccess(response -> log.info("✅ Successfully added MANAGER role to user: {}", response.getEmail()))
                .doOnError(error -> log.error("❌ Failed to add MANAGER role: {}", error.getMessage()));
    }

    private Mono<Employee> updateEmployeeAsManager(Employee employee, Department department) {
        employee.setDepartment(department);
        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee)
                .doOnSuccess(updated -> log.info("✅ Updated employee as manager: {}", updated.getEmail()));
    }
    private Mono<UserResponse> getUpdatedUser(String userEmail, String token, ServerHttpRequest request) {

        String requestUserId = request.getHeaders().getFirst("X-User-Id");
        String adminEmail = request.getHeaders().getFirst("X-User-Email");
        String adminRoles = request.getHeaders().getFirst("X-User-Roles");

        // Get user by email (same as your earlier approach)
        return authWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/user/email")
                        .queryParam("email", userEmail)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", requestUserId)
                .header("X-User-Email", adminEmail)
                .header("X-User-Roles", adminRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .doOnSuccess(user -> log.info("🔍 Retrieved updated user with roles: {}", user.getRoles()));
    }

    public Flux<EmployeeDepartmentResponse> getEmployeesInMyDepartment(String token, ServerHttpRequest request) {
        // Get headers from gateway
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        log.info("🔍 User requesting employees in their department: {}", userEmail);

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
                    // Check if user has Manager, Admin, or SuperAdmin privileges
                    if (!hasManagerOrAdminPrivileges(currentUser.getRoles())) {
                        return Flux.error(new UnauthorizedException(
                                "Access denied: Manager, Admin, or SuperAdmin privileges required"));
                    }

                    // First, check if user has an employee record (regardless of role)
                    return employeeRepository.findByEmail(currentUser.getEmail())
                            .flatMapMany(userEmployee -> {
                                // User has employee record - show their ACTUAL department
                                Department userDepartment = userEmployee.getDepartment();
                                log.info("👤 {} (with employee record) viewing department: {}",
                                        currentUser.getEmail(), userDepartment);

                                return employeeRepository.findByDepartment(userDepartment)
                                        .map(employee -> modelMapper.map(employee, EmployeeDepartmentResponse.class))
                                        .doOnNext(emp -> log.debug("📋 Found employee: {} in department: {}",
                                                emp.getEmail(), emp.getDepartment()));
                            })
                            .switchIfEmpty(Flux.defer(() -> {
                                // User has NO employee record - handle based on role
                                if (currentUser.getRoles().contains(Roles.SUPERADMIN)) {
                                    log.info("👑 SuperAdmin {} (no employee record) viewing default OPERATIONS department",
                                            currentUser.getEmail());
                                    return employeeRepository.findByDepartment(Department.OPERATIONS)
                                            .map(employee -> modelMapper.map(employee, EmployeeDepartmentResponse.class));
                                } else {
                                    // Regular Admin/Manager without employee record - error
                                    return Flux.error(new RuntimeException(
                                            "Employee record not found for: " + currentUser.getEmail()));
                                }
                            }));
                })
                .doOnComplete(() -> log.info("✅ Successfully retrieved employees"));

    }

    private boolean hasManagerOrAdminPrivileges(java.util.List<String> roles) {
        return roles.contains(Roles.MANAGER) ||
                roles.contains(Roles.ADMIN) ||
                roles.contains(Roles.SUPERADMIN);
    }


}