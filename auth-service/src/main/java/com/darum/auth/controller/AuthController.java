package com.darum.auth.controller;

import com.darum.shared.dto.Roles;
import com.darum.shared.dto.request.AddRoleRequest;
import com.darum.shared.dto.response.UserResponse;
import com.darum.auth.model.CustomUserDetails;
import com.darum.auth.model.User;
import com.darum.auth.repositories.UserRepository;
import com.darum.auth.service.AuthService;
import com.darum.auth.dto.request.AuthRequest;
import com.darum.auth.dto.request.RegisterRequest;
import com.darum.shared.dto.response.ApiResponse;
import com.darum.auth.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

   @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
       AuthResponse response = authService.register(registerRequest);
       return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.CREATED);
   }

   @PostMapping("/login")
    public ResponseEntity<?> login (@Valid @RequestBody AuthRequest authRequest) {
       AuthResponse response = authService.login(authRequest);
       return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.OK);
   }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getById(@AuthenticationPrincipal CustomUserDetails userDetail) {
        Optional<User> userFound = userRepository.findByEmail(userDetail.getUsername());
        return userFound
                .map(user -> ResponseEntity.ok(modelMapper.map(user, UserResponse.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/email")
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
       Optional<User> userFound = userRepository.findByEmail(email);
       return userFound
               .map(user -> ResponseEntity.ok(modelMapper.map(user, UserResponse.class)))
               .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<?> addRoleToUser(
            @PathVariable Long userId,
            @RequestBody AddRoleRequest addRoleRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "X-User-Email", required = false) String headerUserEmail
    ) {

        //  Option 1: If called through gateway (with authentication)
        if (currentUser != null) {

            //  USE ROLES CONSTANT
            if (!currentUser.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUPERADMIN))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied: Super Admin privileges required"));
            }

        }
        //  Option 2: If called from another service (with headers)
        else if (headerUserEmail != null) {
            User callingUser = userRepository.findByEmail(headerUserEmail)
                    .orElseThrow(() -> new RuntimeException("Calling user not found"));

            //  USE ROLES CONSTANT
            if (!callingUser.getRoles().contains(Roles.SUPERADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied: Super Admin privileges required"));
            }
        }
        //  No authentication
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required"));
        }

        try {
            UserResponse updatedUser = authService.addRoleToUser(userId, addRoleRequest.getRole());
            return ResponseEntity.ok(new ApiResponse(true,  updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }

    }

    // Remove role - DELETE with specific remove path
    @PostMapping("/users/{userId}/roles/remove")
    public ResponseEntity<?> removeRoleFromUser(
            @PathVariable Long userId,
            @RequestBody AddRoleRequest removeRoleRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "X-User-Email", required = false) String headerUserEmail
    ) {

        // Option 1: If called through gateway (with authentication)
        if (currentUser != null) {
            // USE ROLES CONSTANT
            if (!currentUser.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SUPERADMIN))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied: Super Admin privileges required"));
            }
        }
        // Option 2: If called from another service (with headers)
        else if (headerUserEmail != null) {
            User callingUser = userRepository.findByEmail(headerUserEmail)
                    .orElseThrow(() -> new RuntimeException("Calling user not found"));

            // USE ROLES CONSTANT
            if (!callingUser.getRoles().contains(Roles.SUPERADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Access denied: Super Admin privileges required"));
            }
        }
        // No authentication
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required"));
        }

        try {
            UserResponse updatedUser = authService.removeRoleFromUser(userId, removeRoleRequest.getRole());
            return ResponseEntity.ok(new ApiResponse(true,  updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
