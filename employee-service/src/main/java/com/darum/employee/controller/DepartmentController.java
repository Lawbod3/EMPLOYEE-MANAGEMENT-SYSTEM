package com.darum.employee.controller;

import com.darum.employee.dto.request.UpdateDepartmentRequest;
import com.darum.employee.service.DepartmentService;
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
@RequestMapping("/employees/department")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping("/all")
    public Mono<ResponseEntity<ApiResponse>> getAllDepartments(
            @RequestHeader("Authorization") String authorizationHeader,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Received request to get all departments");

        return departmentService.getAllDepartments(token, request)
                .collectList()
                .map(departments -> {
                    log.info("✅ Successfully retrieved {} departments", departments.size());
                    return ResponseEntity.ok(new ApiResponse(true, departments));
                })
                .onErrorResume(e -> handleError(e, "Get Departments"));
    }

    @PutMapping("/employee/update")
    public Mono<ResponseEntity<ApiResponse>> updateEmployeeDepartment(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateDepartmentRequest updateRequest,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Received update department request for: {} to {}",
                updateRequest.getEmployeeCode(), updateRequest.getDepartment());

        return departmentService.updateEmployeeDepartment(token, updateRequest, request)
                .map(updatedEmployee -> {
                    log.info("✅ Successfully updated {} department to {}",
                            updatedEmployee.getEmail(), updatedEmployee.getDepartment());
                    return ResponseEntity.ok(
                            new ApiResponse(true, updatedEmployee)
                    );
                })
                .onErrorResume(e -> handleError(e, "Department update"));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
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
        } else if (errorMessage.contains("Authentication failed")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (errorMessage.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        } else if (errorMessage.contains("Invalid department") ||
                errorMessage.contains("Employee code is required") ||
                errorMessage.contains("Department is required")) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
