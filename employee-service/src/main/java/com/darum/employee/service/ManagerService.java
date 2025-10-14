package com.darum.employee.service;

import com.darum.employee.dto.request.PromoteToManagerRequest;
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
                                        .then(getUpdatedUser(targetUser.getId(), token, request)) // ← Get user with new ro
                                        .then(Mono.fromCallable(() ->{
                                            EmployeeResponse response = modelMapper.map(targetEmployee, EmployeeResponse.class);
                                             response.setRoles(targetUser.getRoles()); // ← ADD THIS LINE
                                        return response;
                                        }));
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
    private Mono<UserResponse> getUpdatedUser(Long userId, String token, ServerHttpRequest request) {
        String requestUserId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        return authWebClient.get()
                .uri("/auth/users/{userId}/roles", userId) // ← ADD /roles at the end
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", requestUserId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .doOnSuccess(user -> log.info("🔍 Retrieved updated user with roles: {}", user.getRoles()));
    }
}
