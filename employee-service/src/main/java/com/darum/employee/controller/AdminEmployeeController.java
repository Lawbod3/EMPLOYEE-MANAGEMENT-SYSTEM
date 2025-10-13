package com.darum.employee.controller;

import com.darum.employee.dto.request.CreateEmployeeRequest;
import com.darum.employee.service.AdminEmployeeService;
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
public class AdminEmployeeController {
    private final AdminEmployeeService employeeService;

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse>> createEmployee(@RequestHeader("Authorization") String authorization,
                                                                             @Valid @RequestBody CreateEmployeeRequest request, ServerHttpRequest server){
        String token = authorization.replace("Bearer ", "");
        return employeeService.createEmployee(token, request, server)
                .map(employeeResponse ->
                        ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, employeeResponse))
                )
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse(false,e.getMessage()))) // or a proper error response DTO
                );

    }

    @GetMapping("/getAllEmployees")
    public Mono<ResponseEntity<ApiResponse>> getAllEmployees(@RequestHeader("Authorization") String authorizationHeader, ServerHttpRequest request){
        String token = extractToken(authorizationHeader);
        return employeeService.getAllEmployees(token, request)
                .collectList()
                .map(employees -> ResponseEntity.ok(
                        new ApiResponse(true, employees)
                ))
                .onErrorResume(e -> {
                    // Handle different types of errors with appropriate status codes
                    if (e.getMessage().contains("Access denied")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ApiResponse(false, e.getMessage())));
                    } else if (e.getMessage().contains("Authentication failed")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
