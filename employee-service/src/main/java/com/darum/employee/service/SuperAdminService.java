package com.darum.employee.service;

import com.darum.employee.dto.request.PromoteToAdminRequest;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.repositories.EmployeeRepository;
import com.darum.shared.dto.Roles;
import com.darum.shared.dto.request.AddRoleRequest;
import com.darum.shared.dto.response.ApiResponse;
import com.darum.shared.dto.response.UserResponse;
import com.darum.shared.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SuperAdminService {
    private final WebClient authWebClient;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;

    public Mono<EmployeeResponse> promoteToAdmin(String token, PromoteToAdminRequest promoteRequest, ServerHttpRequest request) {
        // Get headers from the incoming request (from gateway)
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        return authWebClient.get()
                .uri("/auth/me")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("Authentication failed: " + e.getMessage()));
                })
                .flatMap(superAdminUser -> {
                    // ✅ FIX: Use same format as AdminEmployeeService (without ROLE_ prefix)
                    if (!superAdminUser.getRoles().contains(Roles.SUPERADMIN)) {
                        return Mono.error(new RuntimeException("Access denied: Super Admin privileges required"));
                    }

                    return employeeRepository.findByEmail(promoteRequest.getEmail())
                            .doOnNext(employee -> System.out.println("🔍 Found employee: " + employee.getEmail()))
                            .switchIfEmpty(Mono.defer(() -> {
                                return Mono.error(new RuntimeException("Employee not found with email: " + promoteRequest.getEmail()));
                            }))
                            .flatMap(targetEmployee -> {
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
                                        .onErrorResume(e -> {
                                            return Mono.error(new RuntimeException("User not found with email: " + promoteRequest.getEmail()));
                                        })
                                        .flatMap(targetUser -> {
                                            return authWebClient.put()
                                                    .uri("/auth/users/" + targetUser.getId() + "/roles")
                                                    .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                                                    .header("X-User-Id", userId)
                                                    .header("X-User-Email", userEmail)
                                                    .header("X-User-Roles", userRoles)
                                                    .bodyValue(new AddRoleRequest(Roles.ADMIN))
                                                    .retrieve()
                                                    .onStatus(status -> status.is4xxClientError(), response -> {
                                                        return response.bodyToMono(ApiResponse.class)
                                                                .flatMap(apiResponse -> {
                                                                    return Mono.error(new RuntimeException(apiResponse.getData().toString()));
                                                                });
                                                    })
                                                    .bodyToMono(UserResponse.class)
                                                    .onErrorResume(e -> {
                                                        return Mono.error(new RuntimeException("Failed to promote user: " + e.getMessage()));
                                                    })
                                                    .thenReturn(targetEmployee);
                                        });
                            })
                            .map(employee -> {
                                return modelMapper.map(employee, EmployeeResponse.class);
                            });
                });
    }

    }



