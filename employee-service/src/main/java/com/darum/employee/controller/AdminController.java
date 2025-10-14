package com.darum.employee.controller;

import com.darum.employee.dto.request.CreateEmployeeRequest;
import com.darum.employee.dto.request.UpdateEmployeeStatusRequest;
import com.darum.employee.service.AdminService;
import com.darum.shared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/employees/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse>> createEmployee(@RequestHeader("Authorization") String authorization,
                                                                             @Valid @RequestBody CreateEmployeeRequest request, ServerHttpRequest server){
        String token = authorization.replace("Bearer ", "");
        return adminService.createEmployee(token, request, server)
                .map(employeeResponse ->
                        ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, employeeResponse))
                )
                .onErrorResume(e ->handleError(e, "Create Employee"));

    }

    @GetMapping("/getAllEmployees")
    public Mono<ResponseEntity<ApiResponse>> getAllEmployees(@RequestHeader("Authorization") String authorizationHeader, ServerHttpRequest request){
        String token = extractToken(authorizationHeader);
        return adminService.getAllEmployees(token, request)
                .collectList()
                .map(employees -> ResponseEntity.ok(
                        new ApiResponse(true, employees)
                ))
                .onErrorResume(e -> handleError(e, "Get All Employees"));
    }


    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }

    @PutMapping("/update-status")
    public Mono<ResponseEntity<ApiResponse>> updateEmployeeStatus(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateEmployeeStatusRequest updateRequest,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Received update employee status request for: {} to {}",
                updateRequest.getEmail(), updateRequest.getStatus());

        return adminService.updateEmployeeStatus(token, updateRequest, request)
                .map(updatedEmployee -> {
                    log.info("✅ Successfully updated {} status to {}",
                            updatedEmployee.getEmail(), updatedEmployee.getStatus());
                    return ResponseEntity.ok(
                            new ApiResponse(true, updatedEmployee)
                    );
                })
                .onErrorResume(e -> handleError(e, "Status update"));
    }

    private Mono<ResponseEntity<ApiResponse>> handleError(Throwable e, String operationType) {
        String errorMessage = e.getMessage();
        log.error("❌ {} failed: {}", operationType, errorMessage);

        HttpStatus status = determineHttpStatus(errorMessage, operationType);

        return Mono.just(ResponseEntity.status(status)
                .body(new ApiResponse(false, errorMessage)));
    }

    private HttpStatus determineHttpStatus(String errorMessage, String operationType) {
        if (errorMessage.contains("Access denied")) {
            return HttpStatus.FORBIDDEN;
        } else if (errorMessage.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        } else if (errorMessage.contains("Invalid department") ||
                errorMessage.contains("Invalid status") ||
                errorMessage.contains("Validation failed")) {
            return HttpStatus.BAD_REQUEST;
        } else if (errorMessage.contains("already exists") ||
                errorMessage.contains("User already exists as an employee")) {
            return HttpStatus.CONFLICT;
        } else if (errorMessage.contains("Authentication failed")) {
            return HttpStatus.UNAUTHORIZED;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

    }
}
