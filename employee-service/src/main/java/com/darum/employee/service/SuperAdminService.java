package com.darum.employee.service;

import com.darum.employee.dto.request.PromoteToAdminRequest;
import com.darum.employee.dto.request.RemoveAdminRequest;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.repositories.EmployeeRepository;
import com.darum.shared.dto.Roles;
import com.darum.shared.dto.request.AddRoleRequest;
import com.darum.shared.dto.response.ApiResponse;
import com.darum.shared.dto.response.UserResponse;
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

@Slf4j
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
                    log.info("Super Admin privileges granted");

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

    // Add this method to your SuperAdminService class
    @Transactional
    public Mono<EmployeeResponse> removeAdminRole(String token, RemoveAdminRequest removeRequest, ServerHttpRequest request) {
        // Get headers from the incoming request (from gateway)
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userEmail = request.getHeaders().getFirst("X-User-Email");
        String userRoles = request.getHeaders().getFirst("X-User-Roles");

        log.info("🔍 Removing ADMIN role from: {}, requested by: {}", removeRequest.getEmail(), userEmail);

        return authWebClient.get()
                .uri("/auth/me")
                .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", userRoles)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Authentication failed: " + e.getMessage())))
                .flatMap(superAdminUser -> {
                    // Only SuperAdmin can remove admin role
                    if (!superAdminUser.getRoles().contains(Roles.SUPERADMIN)) {
                        return Mono.error(new RuntimeException("Access denied: Super Admin privileges required"));
                    }

                    return employeeRepository.findByEmail(removeRequest.getEmail())
                            .doOnNext(employee -> log.info("🔍 Found employee: {}", employee.getEmail()))
                            .switchIfEmpty(Mono.error(new RuntimeException("Employee not found with email: " + removeRequest.getEmail())))
                            .flatMap(targetEmployee -> {
                                return authWebClient.get()
                                        .uri(uriBuilder -> uriBuilder
                                                .path("/auth/user/email")
                                                .queryParam("email", removeRequest.getEmail())
                                                .build())
                                        .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                                        .header("X-User-Id", userId)
                                        .header("X-User-Email", userEmail)
                                        .header("X-User-Roles", userRoles)
                                        .retrieve()
                                        .bodyToMono(UserResponse.class)
                                        .onErrorResume(e -> Mono.error(new RuntimeException("User not found with email: " + removeRequest.getEmail())))
                                        .flatMap(targetUser -> {
                                            // Check if user actually has ADMIN role before removing
                                            if (!targetUser.getRoles().contains(Roles.ADMIN)) {
                                                return Mono.error(new RuntimeException("User does not have ADMIN role: " + removeRequest.getEmail()));
                                            }

                                            // Remove ADMIN role via Auth Service using the new endpoint
                                            return authWebClient.post()
                                                    .uri("/auth/users/" + targetUser.getId() + "/roles/remove")
                                                    .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX + token)
                                                    .header("X-User-Id", userId)
                                                    .header("X-User-Email", userEmail)
                                                    .header("X-User-Roles", userRoles)
                                                    .bodyValue(new AddRoleRequest(Roles.ADMIN))
                                                    .retrieve()
                                                    .onStatus(status -> status.is4xxClientError(), response -> {
                                                        return response.bodyToMono(ApiResponse.class)
                                                                .flatMap(apiResponse -> Mono.error(new RuntimeException(apiResponse.getData().toString())));
                                                    })
                                                    .bodyToMono(UserResponse.class)
                                                    .onErrorResume(e -> Mono.error(new RuntimeException("Failed to remove admin role: " + e.getMessage())))
                                                    .thenReturn(targetEmployee);
                                        });
                            })
                            .map(employee -> modelMapper.map(employee, EmployeeResponse.class));
                });
    }

    }



