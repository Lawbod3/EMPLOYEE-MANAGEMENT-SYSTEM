package com.darum.employee.controller;

import com.darum.employee.documentations.EmployeeApiDocs;
import com.darum.employee.dto.request.GetEmployeeRequest;
import com.darum.employee.service.EmployeeService;
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
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    // POST /employees/specific - Get employee by employeeCode (Admin/SuperAdmin/Manager only)
    @EmployeeApiDocs.GetSpecificEmployeeDoc
    @PostMapping("/get/specific-employee")
    public Mono<ResponseEntity<ApiResponse>> getEmployeeByCode(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody GetEmployeeRequest getEmployeeRequest,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Received request to get employee by code: {}", getEmployeeRequest.getEmployeeCode());

        return employeeService.getEmployeeByCode(token, getEmployeeRequest, request)
                .map(employee -> {
                    log.info("✅ Successfully retrieved employee: {} with code: {}",
                            employee.getEmail(), getEmployeeRequest.getEmployeeCode());
                    return ResponseEntity.ok(new ApiResponse(true, employee));
                })
                .onErrorResume(e -> handleError(e, "Get Employee by Code"));
    }

    // GET /employees/me - View own details (any authenticated employee)
    @EmployeeApiDocs.GetMyDetailsDoc
    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse>> getMyDetails(
            @RequestHeader("Authorization") String authorizationHeader,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Employee requesting own details");

        return employeeService.getMyDetails(token, request)
                .map(employee -> {
                    log.info("✅ Successfully retrieved own details: {}", employee.getEmail());
                    return ResponseEntity.ok(new ApiResponse(true, employee));
                })
                .onErrorResume(e -> handleError(e, "Get My Details"));
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
        } else if (errorMessage.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        } else if (errorMessage.contains("Authentication failed")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (errorMessage.contains("Employee code is required")) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
