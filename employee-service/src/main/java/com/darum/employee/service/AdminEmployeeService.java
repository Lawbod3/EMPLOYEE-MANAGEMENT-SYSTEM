package com.darum.employee.service;

import com.darum.employee.dto.request.CreateEmployeeRequest;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.model.Department;
import com.darum.employee.model.Employee;
import com.darum.employee.model.Status;
import com.darum.employee.repositories.EmployeeRepository;
import com.darum.shared.dto.response.UserResponse;
import com.darum.shared.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.UUID;



@Service
@RequiredArgsConstructor
public class AdminEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final WebClient authWebClient;
    private final ModelMapper modelMapper;

    public Mono<EmployeeResponse> createEmployee(String token , CreateEmployeeRequest createEmployeeRequest, ServerHttpRequest request) {
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
                .flatMap(adminUser -> {
                    // ✅ Check if admin has permission
                    if (!adminUser.getRoles().contains("ADMIN") &&
                            !adminUser.getRoles().contains("SUPERADMIN")) {
                        return Mono.error(new RuntimeException("Access denied"));
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

                    // ✅ Build new employee
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

}
