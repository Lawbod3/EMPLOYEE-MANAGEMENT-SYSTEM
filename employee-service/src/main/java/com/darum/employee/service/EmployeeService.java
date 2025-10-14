package com.darum.employee.service;

import com.darum.employee.dto.request.GetEmployeeRequest;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.exception.EmployeeNotFoundException;
import com.darum.employee.model.Department;
import com.darum.employee.model.Employee;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final WebClient authWebClient;
    private final ModelMapper modelMapper;

    // GET /employees/specific - Get employee by employeeCode only
    public Mono<EmployeeResponse> getEmployeeByCode(String token, GetEmployeeRequest request, ServerHttpRequest serverRequest) {

        String userId = serverRequest.getHeaders().getFirst("X-User-Id");
        String userEmail = serverRequest.getHeaders().getFirst("X-User-Email");
        String userRoles = serverRequest.getHeaders().getFirst("X-User-Roles");


        log.info("🔍 Fetching employee by code: {}, requested by: {}",
                request.getEmployeeCode(), userEmail);

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
                    // Only Admin, SuperAdmin, and Manager can access this endpoint
                    if (!hasEmployeeViewPrivileges(currentUser.getRoles())) {
                        return Mono.error(new UnauthorizedException(
                                "Access denied: Admin, SuperAdmin, or Manager privileges required"));
                    }

                    return employeeRepository.findByEmail(currentUser.getEmail())
                            .switchIfEmpty(Mono.error(new EmployeeNotFoundException("Employee record not found for: " + currentUser.getEmail())))
                            .flatMap(currentEmployee ->
                                    employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                                            .switchIfEmpty(Mono.error(new EmployeeNotFoundException("Employee not found with code: " + request.getEmployeeCode())))
                                            .flatMap(targetEmployee ->
                                                    checkManagerDepartmentAccess(currentUser, currentEmployee, targetEmployee)
                                                            .thenReturn(modelMapper.map(targetEmployee, EmployeeResponse.class))
                                            )
                            );
                });
    }

    // GET /employees/me - View own details (uses logged-in user's email)
    public Mono<EmployeeResponse> getMyDetails(String token, ServerHttpRequest request) {
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");
        String userId = request.getHeaders().getFirst("X-User-Id");

        log.info("🔍 Employee requesting own details: {}", userEmail);

        return authWebClient.get()
                .uri("/auth/me")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Authentication failed: " + e.getMessage())))
                .flatMap(currentUser ->
                        employeeRepository.findByEmail(currentUser.getEmail())
                                .switchIfEmpty(Mono.error(new EmployeeNotFoundException("Employee record not found for: " + currentUser.getEmail())))
                                .map(employee -> modelMapper.map(employee, EmployeeResponse.class))
                );
    }

    private Mono<Void> checkManagerDepartmentAccess(UserResponse currentUser, Employee currentEmployee, Employee targetEmployee) {
        // Admin and SuperAdmin can view any employee
        if (currentUser.getRoles().contains(Roles.SUPERADMIN) ||
                currentUser.getRoles().contains(Roles.ADMIN)) {
            log.info("✅ Admin/SuperAdmin access granted for viewing employee: {}", targetEmployee.getEmail());
            return Mono.empty();
        }

        // Manager can only view employees in their own department
        if (currentUser.getRoles().contains(Roles.MANAGER)) {
            if (currentEmployee.getDepartment() == targetEmployee.getDepartment()) {
                log.info("✅ Manager access granted - same department: {}", currentEmployee.getDepartment());
                return Mono.empty();
            } else {
                log.warn("❌ Manager access denied - different departments. Manager: {}, Target: {}",
                        currentEmployee.getDepartment(), targetEmployee.getDepartment());
                return Mono.error(new UnauthorizedException(
                        "Access denied: You can only view employees in your department (" +
                                currentEmployee.getDepartment() + ")"));
            }
        }

        return Mono.error(new UnauthorizedException("Access denied: Insufficient privileges"));
    }

    private boolean hasEmployeeViewPrivileges(java.util.List<String> roles) {
        return roles.contains(Roles.ADMIN) ||
                roles.contains(Roles.MANAGER) ||
                roles.contains(Roles.SUPERADMIN);
    }



}
