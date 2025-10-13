package com.darum.employee.controller;

import com.darum.employee.dto.request.PromoteToAdminRequest;
import com.darum.employee.service.SuperAdminService;
import com.darum.shared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/employees/admin")
@RequiredArgsConstructor
public class SuperAdminController {
    private final SuperAdminService superAdminService;

    @PutMapping("/promote-to-admin")
    public Mono<ResponseEntity<ApiResponse>> promoteToAdmin(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody PromoteToAdminRequest promoteRequest,
            ServerHttpRequest request) {
        String token = extractToken(authorizationHeader);

        return superAdminService.promoteToAdmin(token, promoteRequest, request)
                .map(promotedEmployee -> ResponseEntity.ok(
                        new ApiResponse(true,  promotedEmployee)
                ))
                .onErrorResume(e -> {
                    if (e.getMessage().contains("Access denied")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ApiResponse(false, e.getMessage())));
                    } else if (e.getMessage().contains("Employee not found")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiResponse(false, e.getMessage())));
                    } else {
                        return Mono.just(ResponseEntity.badRequest()
                                .body(new ApiResponse(false, e.getMessage())));
                    }
                });
    }
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}

