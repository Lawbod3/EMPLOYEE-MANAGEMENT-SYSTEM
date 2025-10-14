package com.darum.employee.controller;

import com.darum.employee.dto.request.DemoteManagerRequest;
import com.darum.employee.dto.request.PromoteToManagerRequest;
import com.darum.employee.service.ManagerService;
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
public class ManagerController {
    private final ManagerService managerService;

    @PutMapping("/promote-to-manager")
    public Mono<ResponseEntity<ApiResponse>> promoteToManager(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody PromoteToManagerRequest promoteRequest,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Received promote to manager request for: {}", promoteRequest.getEmail());

        return managerService.promoteToManager(token, promoteRequest, request)
                .map(promotedEmployee -> {
                    log.info("✅ Successfully promoted {} to manager", promotedEmployee.getEmail());
                    return ResponseEntity.ok(
                            new ApiResponse(true,  promotedEmployee)
                    );
                })
                .onErrorResume(e -> handleError(e, "Promotion"));
    }

    @PutMapping("/demote-manager")
    public Mono<ResponseEntity<ApiResponse>> demoteManager(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody DemoteManagerRequest demoteRequest,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Received demote manager request for: {}", demoteRequest.getEmail());

        return managerService.demoteManager(token, demoteRequest, request)
                .map(demotedEmployee -> {
                    log.info("✅ Successfully demoted manager: {}", demotedEmployee.getEmail());
                    return ResponseEntity.ok(
                            new ApiResponse(true,  demotedEmployee)
                    );
                })
                .onErrorResume(e -> handleError(e, "Demote Manager"));
    }



    @GetMapping("/manager/my-department")
    public Mono<ResponseEntity<ApiResponse>> getEmployeesInMyDepartment(
            @RequestHeader("Authorization") String authorizationHeader,
            ServerHttpRequest request) {

        String token = extractToken(authorizationHeader);
        log.info("🎯 Manager/Admin/SuperAdmin requesting employees in their department");

        return managerService.getEmployeesInMyDepartment(token, request)
                .collectList()
                .map(employees -> {
                    log.info("✅ Successfully retrieved {} employees from user's department", employees.size());
                    return ResponseEntity.ok(
                            new ApiResponse(true, employees)
                    );
                })
                .onErrorResume(e -> handleError(e, "department lookUP"));
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
                errorMessage.contains("Validation failed")) {
            return HttpStatus.BAD_REQUEST;
        } else if (errorMessage.contains("already has role")) {
            return HttpStatus.CONFLICT;
        } else if (errorMessage.contains("Authentication failed")) {
            return HttpStatus.UNAUTHORIZED;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header");
    }
}
