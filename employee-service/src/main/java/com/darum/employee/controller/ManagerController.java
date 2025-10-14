package com.darum.employee.controller;

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
                .onErrorResume(e -> handlePromotionError(e));
    }

    private Mono<ResponseEntity<ApiResponse>> handlePromotionError(Throwable e) {
        String errorMessage = e.getMessage();
        log.error("❌ Promotion failed: {}", errorMessage);

        if (errorMessage.contains("Access denied")) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, errorMessage)));
        } else if (errorMessage.contains("not found")) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, errorMessage)));
        } else if (errorMessage.contains("Invalid department")) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, errorMessage)));
        } else if (errorMessage.contains("already has role")) {
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, errorMessage)));
        } else if (errorMessage.contains("Authentication failed")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, errorMessage)));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred: " + errorMessage)));
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header");
    }
}
